package com.summit.stp.comment.domain.repository;

import com.summit.stp.comment.infrastructure.persistence.po.CommentImagePO;
import java.util.List;

public interface CommentImageRepository {
    CommentImagePO findById(Long id);
    List<CommentImagePO> findByCommentId(Long commentId);
    void save(CommentImagePO commentImage);
    void delete(Long id);
    void deleteByCommentId(Long commentId);
}
