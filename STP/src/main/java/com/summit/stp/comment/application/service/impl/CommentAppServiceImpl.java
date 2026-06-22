package com.summit.stp.comment.application.service.impl;

import cn.hutool.core.util.IdUtil;
import com.summit.stp.comment.application.command.CreateCommentCommand;
import com.summit.stp.comment.application.service.CommentAppService;
import com.summit.stp.comment.application.service.CommentCacheProvider;
import com.summit.stp.comment.domain.model.Comment;
import com.summit.stp.comment.domain.model.CommentImage;
import com.summit.stp.comment.domain.model.CommentType;
import com.summit.stp.comment.domain.repository.CommentImageRepository;
import com.summit.stp.comment.domain.repository.CommentRepository;
import com.summit.stp.comment.infrastructure.persistence.po.CommentsPO;
import com.summit.stp.post.application.service.PostCacheProvider;
import com.summit.stp.post.domain.model.Post;
import com.summit.stp.post.domain.repository.PostRepository;
import com.summit.stp.shared.ThreadContext.UserHolder;
import com.summit.stp.shared.exception.BusinessException;
import com.summit.stp.userAuth.domain.model.UserSession;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import com.summit.stp.shared.constants.BusinessRuleConstants;
import org.springframework.context.ApplicationEventPublisher;
import com.summit.stp.shared.event.FileDeleteEvent;
import com.summit.stp.comment.infrastructure.persistence.po.CommentImagePO;
import com.summit.stp.shared.service.TextSafe.TextSafeServiceProvider;

@Service
@RequiredArgsConstructor
public class CommentAppServiceImpl implements CommentAppService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final PostCacheProvider postCacheProvider;
    private final CommentCacheProvider commentCacheProvider;
    private final CommentImageRepository commentImageRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final TextSafeServiceProvider textSafeServiceProvider;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Comment postComment(CreateCommentCommand command) {
        UserSession user
                = UserHolder.getUser();
        Long postId = command.getPostId();
        if(postId == null){
            throw new IllegalArgumentException("未找到帖子信息");
        }
        Post post = postRepository.findById(postId);
        Comment.Extra extra = command.getExtra();
        Long rootId = command.getRootId();
        long id=  IdUtil.getSnowflakeNextId();
        Long parentId = command.getParentId();
        if(rootId == null){
            parentId = null;
        }

        String content = textSafeServiceProvider.xssFilter(command.getContent());


        checkComment(post,command);


        Comment comment = Comment.builder()
                .content(content)
                .type(command.getType())
                .extra(extra)
                .publisherId(user.getId())
                .postId(postId)
                .replyCount(0L)
                .isAudit(true)
                .isTop(0)
                .clientType("web")
                .createTime(new Timestamp(System.currentTimeMillis()))
                .ipLocation(user.getIp())
                .parentId(parentId)
                .rootId(rootId)
                .postStatus(post.getStatus().getCode())
                .id(id)
                .item(Comment.Item.builder()
                        .isLike(false)
                        .authorIsPraised(false)
                        .authorIsReplied(false)
                        .likeCount(0L)
                        .replyCount(0L)
                        .build()
                )
                .build();
        commentRepository.save(comment);
        //初始化缓存评论池
        commentCacheProvider.init(comment.getId(), 0L,0L);
        postCacheProvider.markReplyChanged(postId);
        if (parentId != null) {
            commentCacheProvider.markReplyChanged(parentId);
        }
        return comment;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long id) {
        Comment comment = commentRepository.findById(id);
        
        List<CommentImagePO> images = commentImageRepository.findByCommentId(id);
        
        commentRepository.delete(id);
        commentImageRepository.deleteByCommentId(id);
        
        if (images != null && !images.isEmpty()) {
            List<String> urls = images.stream()
                    .map(CommentImagePO::getImageUrl)
                    .filter(java.util.Objects::nonNull)
                    .toList();
            if (!urls.isEmpty()) {
                applicationEventPublisher.publishEvent(new FileDeleteEvent(this, urls));
            }
        }
        
        if (comment != null) {
            postCacheProvider.markReplyChanged(comment.getPostId());
            if (comment.getParentId() != null) {
                commentCacheProvider.markReplyChanged(comment.getParentId());
            }
        }
    }

    @Override
    public boolean like(Long commentId) {
        return commentCacheProvider.like(commentId);
    }

    @Override
    public void reply(Long commentId) {
        commentCacheProvider.reply(commentId);
    }

    @Override
    public boolean top(Long id, Long postId) {
        //1,获取帖子信息
        Post post = postRepository.findById(postId);
        if(!post.isActive())throw new BusinessException("帖子不存在或者已经删除");
        //只有帖子的发布者才有权置顶评论
        Long uid = UserHolder.getUser().getId();
        if(!(post.getCreatorId() == uid))throw new BusinessException("无权限");
        //2,获取评论信息
        Comment comment = commentRepository.findById(id);
        if(!comment.getIsAudit())throw new BusinessException("评论正在审核");
        //3,置顶评论,已经置顶则取消置顶
        boolean topState = comment.toggleTop();
        //4,保存
        commentRepository.update(comment);
        return topState;
    }


    private void checkComment( Post post, CreateCommentCommand command){
        String content = command.getContent();
        Comment.Extra extra = command.getExtra();
        //检查根评论是否存在
        if(command.getRootId() != null){
            Comment comment = commentRepository.findById(command.getRootId());
            if(comment == null){
                throw new BusinessException("未找到评论信息");
            }
        }
        if(!StringUtil.isNullOrEmpty(content)){
            if (content.length() > BusinessRuleConstants.Comment.MAX_CONTENT_LENGTH) {
                throw new BusinessException("内容不能超过" + BusinessRuleConstants.Comment.MAX_CONTENT_LENGTH + "字");
            }
        }else if(command.getType() == CommentType.TEXT){
            throw new BusinessException("请输入内容");
        }

        if(post == null){
            throw new BusinessException("未找到帖子信息");
        }


        if(extra != null) {
            List<CommentImage> imageMoments = extra.getImageMoments();
            if(imageMoments == null){
                return;
            }
            if (imageMoments.size() > BusinessRuleConstants.Comment.MAX_IMAGE_NUM) {
                throw new BusinessException("图片数量不能超过" + BusinessRuleConstants.Comment.MAX_IMAGE_NUM + "张");
            }
        }
    }


}
