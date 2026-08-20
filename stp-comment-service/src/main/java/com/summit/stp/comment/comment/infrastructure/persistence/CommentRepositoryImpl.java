package com.summit.stp.comment.comment.infrastructure.persistence;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.comment.comment.application.vo.CommentVO;
import com.summit.stp.comment.comment.domain.model.Comment;
import com.summit.stp.comment.comment.domain.model.CommentType;
import com.summit.stp.comment.comment.domain.repository.CommentRepository;
import com.summit.stp.comment.comment.infrastructure.persistence.mapper.CommentLikeMapper;
import com.summit.stp.comment.comment.infrastructure.persistence.mapper.CommentsMapper;
import com.summit.stp.comment.comment.infrastructure.persistence.po.CommentLikePO;
import com.summit.stp.comment.comment.infrastructure.persistence.po.CommentsPO;
import com.summit.devframeworkdddstarter.repo.AbstractRepository;
import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class CommentRepositoryImpl extends AbstractRepository<Comment, CommentsPO> implements CommentRepository {
    @Autowired
    private   CommentsMapper commentsMapper;
    @Autowired
    private   CommentLikeMapper commentLikeMapper;
    @Autowired
    private  CommentMediaProcessor commentMediaProcessor;

    public CommentRepositoryImpl(BaseMapper<CommentsPO> baseMapper) {
        super(baseMapper);

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
    public List<CommentVO> queryCommentByPostIdWithCursor(String idCursor, String hsCursor, Long postId, Integer limit, Comment.Status status) {
        return commentsMapper.queryCommentByPostIdWithCursor(idCursor, postId, limit, StringUtil.isNullOrEmpty(hsCursor) ? null : Double.valueOf(hsCursor),status.getCode());
    }

    @Override
    public List<CommentVO> queryReplyByCursor(Long cursor, Integer limit, Long postId, Long rootId, Comment.Status status) {
        return commentsMapper.queryReplyByCursor(cursor, limit, postId, rootId,status.getCode());
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
    public Page<Comment> queryPage(Integer pageSize, Integer page, String keyword, Integer status) {
        Page<Comment> res = new Page<>();
        Page<CommentsPO> p = new Page<>(page,pageSize);
        LambdaQueryWrapper<CommentsPO> wrapper = new LambdaQueryWrapper<>();
        if(StrUtil.isNotBlank(keyword))wrapper.likeRight(CommentsPO::getContent,keyword);
        if(status != null)wrapper.eq(CommentsPO::getStatus,status);
        p = getBaseMapper().selectPage(p, wrapper);
        return res.setCurrent(p.getCurrent()).setTotal(p.getTotal()).setRecords(p.getRecords().stream().map(this::toModel).toList());
    }

    @Override
    public List<Comment> findListByIds(List<Long> commentIds, Comment.Status status) {
        return getBaseMapper().selectList(new LambdaQueryWrapper<CommentsPO>()
                .in(CommentsPO::getId, commentIds)
                .eq(CommentsPO::getStatus, status.getCode()))
                .stream().map(this::toModel)
                .toList();
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
        Comment.Status status = comment.getStatus();
        return CommentsPO.builder()
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
                .status(status != null ? status.getCode() : null)
                .reportReason(comment.getReportReason())
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
                .type(commentsPO.getType() != null ? CommentType.fromCode(commentsPO.getType()) : null)
                .content(commentsPO.getContent())
                .createTime(commentsPO.getCreateTime())
                .replyCount(commentsPO.getReplyCount())
                .isTop(commentsPO.getIsTop())
                .status(Comment.Status.fromCode(commentsPO.getStatus()))
                .reportReason(commentsPO.getReportReason())
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

