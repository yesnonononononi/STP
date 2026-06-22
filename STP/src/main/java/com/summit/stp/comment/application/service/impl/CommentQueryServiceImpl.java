package com.summit.stp.comment.application.service.impl;

import com.summit.stp.comment.application.command.CommentReplyQueryCommand;
import com.summit.stp.comment.application.command.QueryCommentCommand;
import com.summit.stp.comment.application.service.CommentCacheProvider;
import com.summit.stp.comment.application.service.CommentQueryService;
import com.summit.stp.comment.application.vo.CommentVO;
import com.summit.stp.comment.domain.model.Comment;
import com.summit.stp.comment.domain.model.CommentImage;
import com.summit.stp.comment.domain.repository.CommentImageRepository;
import com.summit.stp.comment.domain.repository.CommentRepository;
import com.summit.stp.user.application.UserFiller;
import com.summit.stp.user.application.vo.UserSimpleVO;
import com.summit.stp.shared.result.CursorPageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class CommentQueryServiceImpl implements CommentQueryService {
    private final CommentRepository commentRepository;
    private final CommentCacheProvider commentCacheProvider;
    private final UserFiller userFiller;
    private final CommentImageRepository commentImageRepository;

    @Override
    public CursorPageResult<CommentVO> queryCommentByPostIdWithCursor(QueryCommentCommand command) {
        //装配基础数据
        Integer limit = command.getLimit();
        List<CommentVO> list  = assembleExtraData(commentRepository.queryCommentByPostIdWithCursor(String.valueOf(command.getIdCursor()),command.getHsCursor(),command.getPostId(), limit));
        boolean hasMore = list.size() >= limit;
        String nextCursor = null;
        if (!list.isEmpty()) {
            CommentVO lastVO = list.getLast();
            Double hotScore = lastVO.getHotScore();
            nextCursor = (hotScore != null ? hotScore : 0.0) + "_" + lastVO.getId();
        }
        return new CursorPageResult<>(list, nextCursor, hasMore);
    }

    @Override
    public CursorPageResult<CommentVO> queryReplyByCursor(CommentReplyQueryCommand command) {
        List<CommentVO> list = commentRepository.queryReplyByCursor(command.getCursor(), command.getLimit(), command.getPostId(), command.getRootId());
        assembleExtraData(list);
        
        boolean hasMore = list.size() >= command.getLimit();
        String nextCursor = null;
        if (!list.isEmpty()) {
            nextCursor = list.getLast().getId().toString();
        }
        return new CursorPageResult<>(list, nextCursor, hasMore);
    }

    @Override
    public CommentVO queryCommentById(Long commentId) {
        Comment comment = commentRepository.findById(commentId);
        if (comment == null) {
            return null;
        }
        CommentVO vo = CommentVO.builder()
                .id(comment.getId())
                .rootId(comment.getRootId())
                .parentId(comment.getParentId())
                .postId(comment.getPostId())
                .postStatus(comment.getPostStatus() != null && comment.getPostStatus() == 1)
                .isAudit(comment.getIsAudit())
                .type(comment.getType().getCode())
                .content(comment.getContent())
                .createTime(comment.getCreateTime())
                .isTop(comment.getIsTop())
                .status(comment.getStatus())
                .ipLocation(comment.getIpLocation())
                .clientType(comment.getClientType())
                .publisherId(comment.getPublisherId())
                .extra(comment.getExtra())
                .build();
        assembleExtraData(new java.util.ArrayList<>(List.of(vo)));
        return vo;
    }

    private List<CommentVO> assembleExtraData(List<CommentVO> list ){
        if (list == null || list.isEmpty()) {
            return list;
        }
        //装配点赞,回复数据,用户是否点赞,作者是否点赞
        //点赞数和回复数的映射关系
        List<Long> commentIds = list.stream().map(CommentVO::getId).toList();

        Map<Long, Map<String,Long>> countMap = commentCacheProvider.getLikeCountAndReplyCount(commentIds);

        //作者和当前用户是否点赞的映射关系
        Map<Long,Map<String,Boolean>> isLikeMap = commentCacheProvider.getIsLikeAndReplyByIds(list);
        //装配发布者信息
        List<Long> uidList = list.stream().map(CommentVO::getPublisherId).toList();
        Map<Long, UserSimpleVO> userMap = userFiller.fillUsers(uidList);
        list.forEach(vo -> {
            vo.setPublisher(userMap.get(vo.getPublisherId()));
            vo.setItem(Comment.Item.builder()
                            .authorIsPraised(isLikeMap.get(vo.getId()).get(CommentCacheProvider.CREATOR_IS_LIKE))
                            .authorIsReplied(isLikeMap.get(vo.getId()).get(CommentCacheProvider.CREATOR_IS_REPLIED))
                            .isLike(isLikeMap.get(vo.getId()).get(CommentCacheProvider.IS_LIKE))
                            .likeCount(countMap.get(vo.getId()).get(CommentCacheProvider.LIKE_COUNT))
                            .replyCount(countMap.get(vo.getId()).get(CommentCacheProvider.REPLY_COUNT))
                            .build()
                    );
        });
        //装配图片信息

        Map<Long,List<CommentImage>> imgList =  commentImageRepository.queryByCommentIds(commentIds);
        list.forEach(vo -> {
            Comment.Extra extra = vo.getExtra();
            if (extra == null) {
                extra = Comment.Extra.builder().build();
            }
            extra.setImageMoments(imgList.get(vo.getId()));
            vo.setExtra(extra);
        });
        return list;
    }
}
