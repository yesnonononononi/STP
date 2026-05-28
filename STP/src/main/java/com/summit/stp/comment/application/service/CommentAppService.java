package com.summit.stp.comment.application.service;

import com.summit.stp.comment.infrastructure.persistence.po.CommentsPO;
import java.util.List;

public interface CommentAppService {
    CommentsPO getCommentById(Long id);
    void postComment(CommentsPO comment);
    void deleteComment(Long id);
    List<CommentsPO> getCommentsByPostId(Long postId);
}
