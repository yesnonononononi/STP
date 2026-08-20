package com.summit.stp.comment.comment.domain.repository;

import com.summit.stp.comment.comment.domain.model.CommentImage;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CommentImageRepository<T> {
    Optional<T> findById(Long id);
    List<T> findByCommentId(Long commentId);
    void save(CommentImage commentImage);
    void updateById(CommentImage commentImage);
    void delete(Long id);
    void deleteByCommentId(Long commentId);

    Map<Long, List<CommentImage>> queryByCommentIds(List<Long> commentIds);
}
