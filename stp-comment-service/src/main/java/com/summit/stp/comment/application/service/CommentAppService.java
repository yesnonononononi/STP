package com.summit.stp.comment.application.service;

import com.summit.stp.comment.application.command.CreateCommentCommand;
import com.summit.stp.comment.application.vo.CommentVO;

public interface CommentAppService {
    /**
     * 创建评论
     * @param command 创建评论命令
     * @return 组装好的评论VO
     */
    CommentVO postComment(CreateCommentCommand command);
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
     * 置顶
     * @param id 评论id
     * @param postId 帖子id
     */
    boolean top(Long id, Long postId);
}
