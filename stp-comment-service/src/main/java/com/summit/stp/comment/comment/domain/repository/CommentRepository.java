package com.summit.stp.comment.comment.domain.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.comment.comment.application.vo.CommentVO;
import com.summit.stp.comment.comment.domain.model.Comment;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface CommentRepository {
    Optional<Comment> findById(Long id);

    void save(Comment comment);
    void delete(Long id);


    List<CommentVO> queryCommentByPostIdWithCursor(String idCursor,String hsCursor, Long postId, Integer limit, Comment.Status status);

    List<CommentVO> queryReplyByCursor(Long cursor, Integer limit, Long postId, Long rootId, Comment.Status status);

    void saveLike(Long commentId, Long userId);

    void deleteLike(Long commentId, Long userId);

    Set<Long> getLikeUserIds(Long commentId);


    Map<Long, Long> queryReplyCounts(List<Long> commentIds);

    void update(Comment comment);

    List<Comment> queryActiveComments(LocalDateTime deadLine, Timestamp lastUpdateTime, Long lastId);


    void batchUpdateHs(List<Comment> comments);

    Page<Comment> queryPage(Integer pageSize, Integer page, String keyword, Integer status);

    List<Comment> findListByIds(List<Long> commentIds, Comment.Status status);
}
