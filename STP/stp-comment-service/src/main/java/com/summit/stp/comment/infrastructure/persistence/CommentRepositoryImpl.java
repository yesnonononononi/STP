package com.summit.stp.comment.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.summit.stp.comment.application.vo.CommentVO;
import com.summit.stp.comment.domain.model.Comment;
import com.summit.stp.comment.domain.model.CommentType;
import com.summit.stp.comment.domain.repository.CommentRepository;
import com.summit.stp.comment.infrastructure.persistence.mapper.CommentImageMapper;
import com.summit.stp.comment.infrastructure.persistence.mapper.CommentLikeMapper;
import com.summit.stp.comment.infrastructure.persistence.mapper.CommentsMapper;
import com.summit.stp.comment.infrastructure.persistence.po.CommentLikePO;
import com.summit.stp.comment.infrastructure.persistence.po.CommentsPO;
import com.summit.stp.common.application.domain.exception.BusinessException;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.sql.Timestamp;
import java.util.Set;
import java.util.Collections;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepository {
    private final CommentsMapper commentsMapper;
    private final CommentImageMapper commentImageMapper;
    private final CommentLikeMapper commentLikeMapper;
    private final CommentMediaProcessor commentMediaProcessor;


    @Override
    public Comment findById(Long id) {
        CommentsPO commentsPO = commentsMapper.selectOne(
                new LambdaQueryWrapper<CommentsPO>().eq(CommentsPO::getPublicId, id));
        return convertToDomain(commentsPO);
    }

    @Override
    public List<Comment> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<CommentsPO> poList = commentsMapper.selectList(
                new LambdaQueryWrapper<CommentsPO>().in(CommentsPO::getPublicId, ids));
        return poList.stream().map(this::convertToDomain).toList();
    }

    private Comment convertToDomain(CommentsPO commentsPO) {
        Comment.Extra extra = Comment.deserializeExtra(commentsPO.getExtra());
        return Comment.builder()
                .id(commentsPO.getPublicId())
                .rootId(commentsPO.getRootId())
                .publisherId(commentsPO.getUserId())
                .parentId(commentsPO.getParentId())
                .postId(commentsPO.getPostId())
                .isAudit(commentsPO.getIsAudit())
                .type(CommentType.fromCode(commentsPO.getType()))
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(Comment comment) {
        CommentsPO po = convertToPO(comment);
        commentMediaProcessor.handleExtra( comment,po);
        commentsMapper.insert(po);
    }





    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        commentsMapper.delete(new LambdaQueryWrapper<CommentsPO>().eq(CommentsPO::getPublicId, id));
        commentLikeMapper.delete(new LambdaQueryWrapper<CommentLikePO>().eq(CommentLikePO::getCommentId, id));
    }



    @Override
    public List<CommentVO> queryCommentByPostIdWithCursor(String idCursor,String hsCursor, Long postId, Integer limit) {
        return commentsMapper.queryCommentByPostIdWithCursor(idCursor, postId, limit, StringUtil.isNullOrEmpty(hsCursor) ? null : Double.valueOf(hsCursor));
    }

    @Override
    public List<CommentVO> queryReplyByCursor(Long cursor, Integer limit, Long postId, Long rootId) {
        return commentsMapper.queryReplyByCursor(cursor,limit,postId,rootId);
    }

    @Override
    public Long queryLikeCountById(Long commentId) {
        return commentLikeMapper.selectCount(new LambdaQueryWrapper<CommentLikePO>().eq(CommentLikePO::getCommentId, commentId));
    }

    @Override
    public Long queryReplyCountById(Long commentId) {
        return commentsMapper.selectCount(new LambdaQueryWrapper<CommentsPO>().eq(CommentsPO::getParentId, commentId));
    }

    @Override
    public void update(Comment comment) {
        int update = commentsMapper.update(convertToPO(comment),
                new LambdaQueryWrapper<CommentsPO>().eq(CommentsPO::getPublicId, comment.getId()));
        if (update == 0) {
            throw new BusinessException("更新评论失败");
        }
    }

    @Override
    public List<Comment> queryActiveComments(LocalDateTime deadLine,Timestamp lastUpdateTime, Long lastId) {
        List<CommentsPO> commentsPOS = commentsMapper.queryActiveComment(deadLine,lastUpdateTime, lastId);
        return commentsPOS.stream().map(this::convertToDomain).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdate(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) return;
        for (Comment comment : comments) {
            CommentsPO po = convertToPO(comment);
            commentsMapper.update(po,
                    new LambdaUpdateWrapper<CommentsPO>().eq(CommentsPO::getPublicId, comment.getId()));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateHs(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) return;
        for (Comment comment : comments) {
            commentsMapper.update(null,
                    new LambdaUpdateWrapper<CommentsPO>()
                            .eq(CommentsPO::getPublicId, comment.getId())
                            .set(CommentsPO::getHotScore, comment.getItem() != null && comment.getItem().getHotScore() != null ? comment.getItem().getHotScore().longValue() : 0L)
                            .set(CommentsPO::getUpdateTime, comment.getUpdateTime() != null ? comment.getUpdateTime() : new Timestamp(System.currentTimeMillis())));
        }
    }

    private CommentsPO convertToPO(Comment comment) {
        CommentType type = comment.getType();
        return CommentsPO.builder()
                .isAudit(comment.getIsAudit())
                .isTop(comment.getIsTop())
                .type(type == null ? null :type.getCode())
                .clientType(comment.getClientType())
                .createTime(comment.getCreateTime())
                .publicId(comment.getId())
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
        List<Map<String, Object>> rows = commentsMapper.selectMaps(
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
        // Tip: 构造评论点赞的持久化PO对象，并记录当前系统时间戳
        CommentLikePO po = CommentLikePO.builder()
                .commentId(commentId)
                .userId(userId)
                .createTime(new Timestamp(System.currentTimeMillis()))
                .build();
        // Tip: 将点赞记录持久化写入数据库 comment_like 表中
        commentLikeMapper.insert(po);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLike(Long commentId, Long userId) {
        // Tip: 根据评论ID与点赞人用户ID在数据库中删除对应的点赞行记录
        commentLikeMapper.delete(new LambdaQueryWrapper<CommentLikePO>()
                .eq(CommentLikePO::getCommentId, commentId)
                .eq(CommentLikePO::getUserId, userId));
    }

    @Override
    public Set<Long> getLikeUserIds(Long commentId) {
        // Tip: 批量查询数据库中点赞此评论的所有记录
        List<CommentLikePO> list = commentLikeMapper.selectList(new LambdaQueryWrapper<CommentLikePO>()
                .eq(CommentLikePO::getCommentId, commentId));
        if (list == null) {
            // Tip: 查无记录时返回安全的空 Set，避免外部 NPE
            return Collections.emptySet();
        }
        // Tip: 将点赞记录流转换为点赞用户ID的 Set 集合并返回
        return list.stream()
                .map(CommentLikePO::getUserId)
                .collect(Collectors.toSet());
    }

}
