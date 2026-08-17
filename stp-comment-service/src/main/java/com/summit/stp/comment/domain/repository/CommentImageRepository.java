package com.summit.stp.comment.domain.repository;

import com.summit.stp.comment.domain.model.CommentImage;
import com.summit.stp.comment.infrastructure.persistence.po.CommentImagePO;

import java.util.List;
import java.util.Map;

public interface CommentImageRepository<T> {
    T findPOById(Long id);
    List<T> findByCommentId(Long commentId);
    void save(CommentImagePO commentImage);
    void delete(Long id);
    void deleteByCommentId(Long commentId);

    Map<Long, List<CommentImage>> queryByCommentIds(List<Long> commentIds);
}
