package com.summit.stp.comment.application.service;

import com.summit.stp.comment.application.command.CreateCommentCommand;
import com.summit.stp.comment.domain.model.Comment;

public interface CommentAppService {
    /**
     * 创建评论
     * @param command 创建评论命令
     * @return 创建的评论实体
     */
    Comment postComment(CreateCommentCommand command);
    /**
     * 删除评论
     * @param id 评论id
     */
    void deleteComment(Long id);
    /**
     * 点赞
     * @param commentId 评论id
     * @return 点赞状态 true为点赞成功, false为取消点赞成功
     */
    boolean like(Long commentId);
    /**
     * 回复
     * @param commentId 评论id
     */
    void reply(Long commentId);
    /**
     * 置顶
     * @param id 评论id
     * @param postId 帖子id
     */
    boolean top(Long id, Long postId);
}
