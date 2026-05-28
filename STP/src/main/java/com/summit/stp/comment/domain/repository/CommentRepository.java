package com.summit.stp.comment.domain.repository;

import com.summit.stp.comment.infrastructure.persistence.po.CommentsPO;
import java.util.List;

public interface CommentRepository {
    CommentsPO findById(Long id);
    void save(CommentsPO comment);
    void delete(Long id);
    List<CommentsPO> findByPostId(Long postId);
}
