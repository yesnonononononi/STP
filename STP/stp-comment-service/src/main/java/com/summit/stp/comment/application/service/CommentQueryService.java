package com.summit.stp.comment.application.service;

import com.summit.stp.comment.application.command.CommentReplyQueryCommand;
import com.summit.stp.comment.application.command.QueryCommentCommand;
import com.summit.stp.comment.application.vo.CommentVO;
import com.summit.stp.common.application.vo.CommentSimpleVO;

import com.summit.stp.common.result.CursorPageResult;
import java.util.List;

public interface CommentQueryService {
    /**
     * 获取帖子下的评论根据评论id(游标)
     * @param cursor 评论id
     * @param postId 帖子id
     * @param limit 获取数量
     * @return 评论列表
     */
    CursorPageResult<CommentVO> queryCommentByPostIdWithCursor(QueryCommentCommand command);
    /**
     * 获取评论下的回复根据回复id(游标)
     * @return 回复列表
     */
    CursorPageResult<CommentVO> queryReplyByCursor(CommentReplyQueryCommand command);
    /**
     * 根据评论id获取单个评论的展示实体
     * @param commentId 评论id
     * @return 评论展示VO
     */
    CommentVO queryCommentById(Long commentId);

    /**
     * 查询评论简要信息（含当前用户点赞状态），供内部 Feign 调用
     * @param commentId 评论id
     * @return 评论简要 VO，查不到返回 null
     */
    CommentSimpleVO querySimpleCommentWithLikeStatus(Long commentId);

    /**
     * 批量查询评论简要信息（含当前用户点赞状态），供内部 Feign 调用
     * @param commentIds 评论id列表
     * @return 评论简要 VO 列表，查不到的评论不会出现在结果中
     */
    List<CommentSimpleVO> querySimpleCommentsWithLikeStatus(List<Long> commentIds);
}
