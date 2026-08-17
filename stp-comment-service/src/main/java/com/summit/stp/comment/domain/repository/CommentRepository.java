package com.summit.stp.comment.domain.repository;

import com.summit.stp.comment.application.vo.CommentVO;
import com.summit.stp.comment.domain.model.Comment;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface CommentRepository {
    Optional<Comment> findById(Long id);
    List<Comment> findByIds(List<Long> ids);
    void save(Comment comment);
    void delete(Long id);


    List<CommentVO> queryCommentByPostIdWithCursor(String idCursor,String hsCursor, Long postId, Integer limit);

    List<CommentVO> queryReplyByCursor(Long cursor, Integer limit, Long postId, Long rootId);

    Long queryLikeCountById(Long commentId);

    Long queryReplyCountById(Long commentId);

    void saveLike(Long commentId, Long userId);

    void deleteLike(Long commentId, Long userId);

    Set<Long> getLikeUserIds(Long commentId);

    Map<Long, Long> queryLikeCounts(List<Long> commentIds);

    Map<Long, Long> queryReplyCounts(List<Long> commentIds);

    void update(Comment comment);

    List<Comment> queryActiveComments(LocalDateTime deadLine, Timestamp lastUpdateTime, Long lastId);

    void batchUpdate(List<Comment> comments);

    void batchUpdateHs(List<Comment> comments);
}
