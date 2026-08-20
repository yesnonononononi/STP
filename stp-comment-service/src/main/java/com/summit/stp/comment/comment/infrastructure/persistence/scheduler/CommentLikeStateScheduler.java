package com.summit.stp.comment.comment.infrastructure.persistence.scheduler;

import com.summit.stp.comment.comment.application.service.CommentCacheProvider;
import com.summit.stp.comment.comment.domain.model.Comment;
import com.summit.stp.comment.comment.domain.repository.CommentRepository;
import com.summit.stp.comment.comment.infrastructure.constants.CommentConstants;
import com.summit.stp.common.util.DistributedLockUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentLikeStateScheduler {
    private final CommentCacheProvider commentCacheProvider;
    private final CommentRepository commentRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final DistributedLockUtil distributedLockUtil;

    @Scheduled(cron = "0 0/3 * * * ?")
    public void updateCommentLikeState() {

        distributedLockUtil.executeWithLock(CommentConstants.Cache.CHANGED_LOCK, this::conduct);
    }

    private void conduct() {
        // Tip: 1. 扫描崩溃遗留下来的备份 Key，优先处理未完任务（断点续传）
        Set<String> leftoverKeys = redisTemplate.keys(CommentConstants.Cache.CHANGED_RUN_PREFIX + "*");
        if (leftoverKeys != null && !leftoverKeys.isEmpty()) {
            for (String keyObj : leftoverKeys) {
                if (keyObj != null) {
                    processBackupKey(keyObj);
                }
            }
        }

        // Tip: 2. 处理当前新变动。重命名原 key 到临时 backup 键，避免新产生的点赞操作丢失
        String originalKey = CommentConstants.Cache.CHANGED;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(originalKey))) {
            String backupKey = CommentConstants.Cache.CHANGED_RUN_PREFIX + UUID.randomUUID();
            try {
                redisTemplate.rename(originalKey, backupKey);
                processBackupKey(backupKey);
            } catch (Exception ignored) {
                // Tip: 重命名失败说明可能有其他节点在这期间已经执行了该操作，安全跳过
            }
        }
    }

    private void processBackupKey(String backupKey) {
        Set<Object> changedCommentIds = redisTemplate.opsForSet().members(backupKey);
        if (changedCommentIds == null || changedCommentIds.isEmpty()) {
            redisTemplate.delete(backupKey);
            return;
        }

        for (Object obj : changedCommentIds) {
            Long commentId = toLong(obj);
            if (commentId == null) {
                redisTemplate.opsForSet().remove(backupKey, obj);
                continue;
            }

            try {
                // Tip: 3. 强制预热防御：防止缓存被淘汰变成空集后，误删数据库已有的记录
                commentCacheProvider.loadCache(commentId);

                // Tip: 4. 同步最新的点赞关系到数据库
                syncLikes(commentId);

                // Tip: 5. 处理成功一个，从备份 Key 中剔除一个，实现元素级的断点恢复
                redisTemplate.opsForSet().remove(backupKey, obj);
            } catch (Exception e) {
                log.error("【定时评论点赞同步】同步评论失败 commentId={}", commentId, e);
            }
        }

        // Tip: 6. 同步全部完成后，彻底删除本轮备份 Key
        Long size = redisTemplate.opsForSet().size(backupKey);
        if (size == null || size == 0) {
            redisTemplate.delete(backupKey);
        }
    }

    private void syncLikes(Long commentId) {
        // Tip: 获取 Redis 缓存中的点赞用户 ID 集合
        Set<Long> redisLikes = commentCacheProvider.getLikeUserIds(commentId);
        // Tip: 从数据库批量获取目前已有的点赞用户 ID 集合
        Set<Long> dbLikes = commentRepository.getLikeUserIds(commentId);
        Long changeCount = 0L;
        // Tip: 求差集以计算需要新增入库的点赞记录 (Redis 中有，DB 中无)
        Set<Long> toAdd = new HashSet<>(redisLikes);
        toAdd.removeAll(dbLikes);
        changeCount += toAdd.size();
        for (Long userId : toAdd) {
            try {
                commentRepository.saveLike(commentId, userId);
            } catch (Exception e) {
                log.warn("【定时评论点赞同步】插入点赞失败: commentId={}, userId={}", commentId, userId, e);
            }
        }

        // Tip: 求差集以计算需要从 DB 删除的取消点赞记录 (DB 中有，Redis 中无)
        Set<Long> toRemove = new HashSet<>(dbLikes);
        toRemove.removeAll(redisLikes);
        changeCount += toRemove.size();
        for (Long userId : toRemove) {
            try {
                commentRepository.deleteLike(commentId, userId);
            } catch (Exception e) {
                log.warn("【定时评论点赞同步】删除点赞失败: commentId={}, userId={}", commentId, userId, e);
            }
        }

        if (changeCount > 0) {
            long totalLikes = redisLikes.size();
            commentRepository.update(Comment.builder().id(commentId).likeCount(totalLikes).build());
        }
    }

    private Long toLong(Object obj) {
        if (obj instanceof Long value) return value;
        if (obj instanceof Integer value) return value.longValue();
        if (obj instanceof String value) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }
}
