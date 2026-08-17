package com.summit.stp.comment.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.stp.comment.application.vo.CommentVO;
import com.summit.stp.comment.domain.model.Comment;
import com.summit.stp.comment.domain.model.CommentType;
import com.summit.stp.comment.domain.repository.CommentRepository;
import com.summit.stp.comment.infrastructure.persistence.mapper.CommentLikeMapper;
import com.summit.stp.comment.infrastructure.persistence.mapper.CommentsMapper;
import com.summit.stp.comment.infrastructure.persistence.po.CommentLikePO;
import com.summit.stp.comment.infrastructure.persistence.po.CommentsPO;
import com.summit.devframeworkdddstarter.repo.AbstractRepository;
import io.netty.util.internal.StringUtil;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class CommentRepositoryImpl extends AbstractRepository<Comment, CommentsPO> implements CommentRepository {
    private final CommentsMapper commentsMapper;
    private final CommentLikeMapper commentLikeMapper;
    private final CommentMediaProcessor commentMediaProcessor;

    public CommentRepositoryImpl(BaseMapper<CommentsPO> baseMapper,
                                 CommentsMapper commentsMapper,
                                 CommentLikeMapper commentLikeMapper,
                                 CommentMediaProcessor commentMediaProcessor) {
        super(baseMapper);
        this.commentsMapper = commentsMapper;
        this.commentLikeMapper = commentLikeMapper;
        this.commentMediaProcessor = commentMediaProcessor;
    }

    @Override
    public List<Comment> findByIds(List<Long> ids) {
        return findListIn(ids, CommentsPO::getId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(Comment comment) {
        if (comment == null) return;
        CommentsPO po = toPO(comment);
        commentMediaProcessor.handleExtra(comment, po);
        getBaseMapper().insert(po);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        delete(id, CommentsPO::getId);
        commentLikeMapper.delete(new LambdaQueryWrapper<CommentLikePO>().eq(CommentLikePO::getCommentId, id));
    }

    @Override
    public List<CommentVO> queryCommentByPostIdWithCursor(String idCursor, String hsCursor, Long postId, Integer limit) {
        return commentsMapper.queryCommentByPostIdWithCursor(idCursor, postId, limit, StringUtil.isNullOrEmpty(hsCursor) ? null : Double.valueOf(hsCursor));
    }

    @Override
    public List<CommentVO> queryReplyByCursor(Long cursor, Integer limit, Long postId, Long rootId) {
        return commentsMapper.queryReplyByCursor(cursor, limit, postId, rootId);
    }

    @Override
    public Long queryLikeCountById(Long commentId) {
        return commentLikeMapper.selectCount(new LambdaQueryWrapper<CommentLikePO>().eq(CommentLikePO::getCommentId, commentId));
    }

    @Override
    public Long queryReplyCountById(Long commentId) {
        return getBaseMapper().selectCount(new LambdaQueryWrapper<CommentsPO>().eq(CommentsPO::getParentId, commentId));
    }

    @Override
    public void update(Comment comment) {
        if (comment == null) return;
        updateById(comment);
    }

    @Override
    public List<Comment> queryActiveComments(LocalDateTime deadLine, Timestamp lastUpdateTime, Long lastId) {
        List<CommentsPO> commentsPOS = commentsMapper.queryActiveComment(deadLine, lastUpdateTime, lastId);
        return commentsPOS.stream().map(this::toModel).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdate(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) return;
        update(comments);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateHs(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) return;
        for (Comment comment : comments) {
            getBaseMapper().update(null,
                    new LambdaUpdateWrapper<CommentsPO>()
                            .eq(CommentsPO::getId, comment.getId())
                            .set(CommentsPO::getHotScore, comment.getItem() != null && comment.getItem().getHotScore() != null ? comment.getItem().getHotScore().longValue() : 0L)
                            .set(CommentsPO::getUpdateTime, comment.getUpdateTime() != null ? comment.getUpdateTime() : new Timestamp(System.currentTimeMillis())));
        }
    }

    @Override
    public Map<Long, Long> queryLikeCounts(List<Long> commentIds) {
        Map<Long, Long> map = new HashMap<>(commentIds.size());
        if (commentIds.isEmpty()) return map;
        List<Map<String, Object>> rows = commentLikeMapper.selectMaps(
                new QueryWrapper<CommentLikePO>()
                        .select("comment_id as commentId", "count(1) as likeCount")
                        .in("comment_id", commentIds)
                        .groupBy("comment_id")
        );
        if (rows != null) {
            for (Map<String, Object> row : rows) {
                Object commentIdObj = row.get("commentId");
                Object likeCountObj = row.get("likeCount");
                if (commentIdObj instanceof Number commentIdNum) {
                    map.put(commentIdNum.longValue(), likeCountObj instanceof Number number ? number.longValue() : 0L);
                }
            }
        }
        return map;
    }

    @Override
    public Map<Long, Long> queryReplyCounts(List<Long> commentIds) {
        Map<Long, Long> map = new HashMap<>(commentIds.size());
        if (commentIds.isEmpty()) return map;
        List<Map<String, Object>> rows = getBaseMapper().selectMaps(
                new QueryWrapper<CommentsPO>()
                        .select("root_id as rootId", "count(1) as replyCount")
                        .in("root_id", commentIds)
                        .groupBy("root_id")
        );
        if (rows != null) {
            for (Map<String, Object> row : rows) {
                Object parentIdObj = row.get("rootId");
                Object replyCountObj = row.get("replyCount");
                if (parentIdObj instanceof Number parentIdNum) {
                    map.put(parentIdNum.longValue(), replyCountObj instanceof Number number ? number.longValue() : 0L);
                }
            }
        }
        return map;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveLike(Long commentId, Long userId) {
        CommentLikePO po = CommentLikePO.builder()
                .commentId(commentId)
                .userId(userId)
                .createTime(new Timestamp(System.currentTimeMillis()))
                .build();
        commentLikeMapper.insert(po);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLike(Long commentId, Long userId) {
        commentLikeMapper.delete(new LambdaQueryWrapper<CommentLikePO>()
                .eq(CommentLikePO::getCommentId, commentId)
                .eq(CommentLikePO::getUserId, userId));
    }

    @Override
    public Set<Long> getLikeUserIds(Long commentId) {
        List<CommentLikePO> list = commentLikeMapper.selectList(new LambdaQueryWrapper<CommentLikePO>()
                .eq(CommentLikePO::getCommentId, commentId));
        if (list == null) {
            return Collections.emptySet();
        }
        return list.stream()
                .map(CommentLikePO::getUserId)
                .collect(Collectors.toSet());
    }

    @Override
    protected CommentsPO toPO(Comment comment) {
        if (comment == null) return null;
        CommentType type = comment.getType();
        return CommentsPO.builder()
                .isAudit(comment.getIsAudit())
                .isTop(comment.getIsTop())
                .type(type == null ? null : type.getCode())
                .clientType(comment.getClientType())
                .createTime(comment.getCreateTime())
                .id(comment.getId())
                .extra(comment.getExtraJsonString())
                .ipLocation(comment.getIpLocation())
                .postId(comment.getPostId())
                .likeCount(comment.getLikeCount())
                .userId(comment.getPublisherId())
                .updateTime(comment.getUpdateTime() != null ? comment.getUpdateTime() : comment.getCreateTime())
                .rootId(comment.getRootId())
                .parentId(comment.getParentId())
                .replyCount(comment.getReplyCount())
                .status(comment.getStatus())
                .content(comment.getContent())
                .hotScore(comment.getItem() != null && comment.getItem().getHotScore() != null ? comment.getItem().getHotScore().longValue() : 0L)
                .build();
    }

    @Override
    protected Comment toModel(CommentsPO commentsPO) {
        if (commentsPO == null) return null;
        Comment.Extra extra = Comment.deserializeExtra(commentsPO.getExtra());
        return Comment.builder()
                .id(commentsPO.getId())
                .rootId(commentsPO.getRootId())
                .publisherId(commentsPO.getUserId())
                .parentId(commentsPO.getParentId())
                .postId(commentsPO.getPostId())
                .isAudit(commentsPO.getIsAudit())
                .type(commentsPO.getType() != null ? CommentType.fromCode(commentsPO.getType()) : null)
                .content(commentsPO.getContent())
                .createTime(commentsPO.getCreateTime())
                .replyCount(commentsPO.getReplyCount())
                .isTop(commentsPO.getIsTop())
                .status(commentsPO.getStatus())
                .updateTime(commentsPO.getUpdateTime())
                .likeCount(commentsPO.getLikeCount())
                .extra(extra)
                .item(Comment.Item.builder()
                        .likeCount(commentsPO.getLikeCount() != null ? commentsPO.getLikeCount() : 0L)
                        .replyCount(commentsPO.getReplyCount() != null ? commentsPO.getReplyCount() : 0L)
                        .hotScore(commentsPO.getHotScore() != null ? commentsPO.getHotScore().doubleValue() : 0.0)
                        .isLike(false)
                        .authorIsPraised(false)
                        .authorIsReplied(false)
                        .build())
                .build();
    }
}

