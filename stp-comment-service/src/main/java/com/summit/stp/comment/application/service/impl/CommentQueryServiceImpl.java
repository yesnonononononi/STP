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
import com.summit.stp.common.auth.UserHolder;
import com.summit.stp.comment.api.vo.CommentSimpleVO;
import com.summit.stp.user.api.vo.UserSimpleVO;
import com.summit.stp.user.api.client.UserFeignClient;
import com.summit.stp.common.application.api.result.CursorPageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class CommentQueryServiceImpl implements CommentQueryService {
    private final CommentRepository commentRepository;
    private final CommentCacheProvider commentCacheProvider;
    private final UserFeignClient userFeignClient;
    private final CommentImageRepository commentImageRepository;

    @Override
    public CursorPageResult<CommentVO> queryCommentByPostIdWithCursor(QueryCommentCommand command) {
        //装配基础数据
        Integer limit = command.getLimit();
        Long idCursor = command.getIdCursor();
        List<CommentVO> voList = commentRepository.queryCommentByPostIdWithCursor(idCursor == null ? null : idCursor.toString(), command.getHsCursor(), command.getPostId(), limit);
        List<CommentVO> list  = assembleExtraData(voList);
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
        Comment comment = commentRepository.findById(commentId).orElse(null);
        if (comment == null) {
            return null;
        }
        CommentVO vo = CommentVO.fromModel(comment);
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
        Map<Long, UserSimpleVO> userMap = userFeignClient.findSimpleUserByIds(uidList).getData();
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

    @Override
    public CommentSimpleVO querySimpleCommentWithLikeStatus(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElse(null);
        if (comment == null) {
            return null;
        }

        // 检查当前用户是否已点赞该评论
        Boolean isLiked = checkCurrentUserLiked(commentId);
        return toSimpleVO(comment, isLiked);
    }

    @Override
    public List<CommentSimpleVO> querySimpleCommentsWithLikeStatus(List<Long> commentIds) {
        if (commentIds == null || commentIds.isEmpty()) {
            return List.of();
        }

        // 批量从 DB 查询评论
        List<Comment> comments = commentRepository.findByIds(commentIds);
        if (comments.isEmpty()) {
            return List.of();
        }

        // 批量预热点赞缓存
        commentCacheProvider.loadCache(commentIds);

        // 获取当前用户 ID，用于批量判断点赞状态
        Long currentUserId;
        try {
            currentUserId = UserHolder.getUser().getId();
        } catch (Exception e) {
            currentUserId = null;
        }

        final Long uid = currentUserId;
        return comments.stream().map(comment -> {
            Boolean isLiked = false;
            if (uid != null) {
                isLiked = commentCacheProvider.getLikeUserIds(comment.getId()).contains(uid);
            }
            return toSimpleVO(comment, isLiked);
        }).toList();
    }

    private Boolean checkCurrentUserLiked(Long commentId) {
        try {
            Long currentUserId = UserHolder.getUser().getId();
            if (currentUserId != null) {
                commentCacheProvider.loadCache(commentId);
                return commentCacheProvider.getLikeUserIds(commentId).contains(currentUserId);
            }
        } catch (Exception e) {
            log.debug("无法获取用户点赞状态, commentId={}", commentId, e);
        }
        return false;
    }

    private CommentSimpleVO toSimpleVO(Comment comment, Boolean isLiked) {
        return CommentSimpleVO.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .parentId(comment.getParentId())
                .publisherId(comment.getPublisherId())
                .content(comment.getContent())
                .type(comment.getType() != null ? comment.getType().getCode() : null)
                .isLiked(isLiked)
                .build();
    }
}

