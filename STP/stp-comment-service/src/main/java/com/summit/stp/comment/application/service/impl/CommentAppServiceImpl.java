package com.summit.stp.comment.application.service.impl;

import cn.hutool.core.util.IdUtil;
import com.summit.stp.comment.application.command.CreateCommentCommand;
import com.summit.stp.comment.application.service.CommentCacheProvider;
import com.summit.stp.comment.domain.model.Comment;
import com.summit.stp.comment.domain.model.CommentImage;
import com.summit.stp.comment.domain.model.CommentType;
import com.summit.stp.comment.domain.repository.CommentImageRepository;
import com.summit.stp.comment.domain.repository.CommentRepository;
import com.summit.stp.common.ThreadContext.UserHolder;
import com.summit.stp.common.application.domain.exception.BusinessException;
import com.summit.stp.common.application.domain.model.UserSession;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import com.summit.stp.comment.infrastructure.constants.CommentConstants;
import org.springframework.context.ApplicationEventPublisher;
import com.summit.stp.common.application.domain.event.FileDeleteEvent;
import com.summit.stp.comment.infrastructure.persistence.po.CommentImagePO;
import com.summit.stp.common.application.service.TextSafe.TextSafeServiceProvider;
import com.summit.stp.comment.application.vo.CommentVO;
import com.summit.stp.common.feign.PostFeignClient;
import com.summit.stp.common.feign.UserFeignClient;
import com.summit.stp.common.application.vo.PostSimpleVO;
import com.summit.stp.common.application.vo.UserSimpleVO;
import com.summit.stp.comment.application.service.AbstractCommentAppService;
import com.summit.stp.common.application.domain.event.CommentNotificationMessage;

import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentAppServiceImpl extends AbstractCommentAppService {
    private final CommentRepository commentRepository;
    private final PostFeignClient postFeignClient;
    private final CommentCacheProvider commentCacheProvider;
    private final CommentImageRepository commentImageRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final TextSafeServiceProvider textSafeServiceProvider;
    private final UserFeignClient userFeignClient;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommentVO postComment(CreateCommentCommand command) {
        UserSession user = UserHolder.getUser();
        Long postId = command.getPostId();
        if(postId == null){
            throw new IllegalArgumentException("未找到帖子信息");
        }
        PostSimpleVO post = postFeignClient.findSimplePostById(postId).getData();
        Comment.Extra extra = command.getExtra();
        long id = IdUtil.getSnowflakeNextId();

        // 服务端根据 parentId 推断 rootId，不信任前端传入的 rootId
        Long parentId = command.getParentId();
        Long rootId = null;
        if (parentId != null) {
            Comment parentComment = commentRepository.findById(parentId);
            if (parentComment == null) {
                throw new BusinessException("未找到父评论信息");
            }
            // 父评论是根评论 → rootId = parentId；父评论是回复 → rootId = 父评论的 rootId
            rootId = parentComment.getRootId() != null ? parentComment.getRootId() : parentComment.getId();
        }

        String content = textSafeServiceProvider.xssFilter(command.getContent());

        checkComment(post, command);

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
                .postStatus(post.getStatus())
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
        initCommentCache(comment.getId());
        postFeignClient.incrReplyCount(postId);
        if (parentId != null) {
            commentCacheProvider.reply(parentId);
            commentCacheProvider.markReplyChanged(parentId);
            // 二级回复时，根评论的总回复数也需要增加
            if (rootId != null && !rootId.equals(parentId)) {
                commentCacheProvider.reply(rootId);
                commentCacheProvider.markReplyChanged(rootId);
            }
        }

        UserSimpleVO publisherVO = userFeignClient.findSimpleUserById(user.getId()).getData();
        try {
            String displayContent = resolveContent(comment);

            CommentNotificationMessage msg = CommentNotificationMessage.builder()
                    .commentId(comment.getId())
                    .postId(post.getId())
                    .parentId(comment.getParentId())
                    .commentContent(displayContent)
                    .triggerUserId(user.getId())
                    .postTitle(post.getTitle())
                    .postCreatorId(post.getCreatorId())
                    .clientIp(user.getIp())
                    .timestamp(System.currentTimeMillis())
                    .build();
            pushCommentNotification(msg);
        } catch (Exception e) {
            log.error("【评论模块】推送评论消息到 MQ 异常, commentId={}", comment.getId(), e);
        }
        CommentVO vo = CommentVO.fromModel(comment);
        vo.setPublisher(publisherVO);
        return vo;
    }

    private  String resolveContent(Comment comment) {
        String displayContent = comment.getContent();
        if (comment.getType() != null) {
            if (comment.getType() == CommentType.IMAGE) {
                displayContent = "[图片] " + (displayContent != null ? displayContent : "");
            } else if (comment.getType() == CommentType.VIDEO) {
                displayContent = "[视频] " + (displayContent != null ? displayContent : "");
            } else if (comment.getType() == CommentType.AUDIO) {
                displayContent = "[音频] " + (displayContent != null ? displayContent : "");
            }
        }
        return displayContent;
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
            postFeignClient.decrReplyCount(comment.getPostId());
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
    public boolean top(Long id, Long postId) {
        //1,获取帖子信息
        PostSimpleVO post = postFeignClient.findSimplePostById(postId).getData();
        if(post == null || post.getStatus() == null || post.getStatus() != 1) throw new BusinessException("帖子不存在或者已经删除");
        //只有帖子的发布者才有权置顶评论
        Long uid = UserHolder.getUser().getId();
        if(!(post.getCreatorId().equals(uid)))throw new BusinessException("无权限");
        //2,获取评论信息
        Comment comment = commentRepository.findById(id);
        if(!comment.getIsAudit())throw new BusinessException("评论正在审核");
        //3,置顶评论,已经置顶则取消置顶
        boolean topState = comment.toggleTop();
        //4,保存
        commentRepository.update(comment);
        return topState;
    }


    private void checkComment(PostSimpleVO post, CreateCommentCommand command){
        String content = command.getContent();
        Comment.Extra extra = command.getExtra();
        // parentId / rootId 已在 postComment 中校验，此处不再重复查库
        if (post == null) {
            throw new BusinessException("未找到帖子信息");
        }
        if(!StringUtil.isNullOrEmpty(content)){
            if (content.length() > CommentConstants.Business.MAX_CONTENT_LENGTH) {
                throw new BusinessException("内容不能超过" + CommentConstants.Business.MAX_CONTENT_LENGTH + "字");
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
            if (imageMoments.size() > CommentConstants.Business.MAX_IMAGE_NUM) {
                throw new BusinessException("图片数量不能超过" + CommentConstants.Business.MAX_IMAGE_NUM + "张");
            }
        }
    }

    /**
     * 初始化评论缓存池
     */
    private void initCommentCache(Long commentId) {
        commentCacheProvider.init(commentId, 0L, 0L);
        log.info("[【评论模块】初始化缓存] commentId={}", commentId);
    }



}
