package com.summit.stp.post.infrastructure.persistence.scheduler;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.summit.stp.comment.infrastructure.persistence.mapper.CommentsMapper;
import com.summit.stp.post.infrastructure.persistence.mapper.PostsMapper;
import com.summit.stp.post.infrastructure.persistence.po.PostsPO;
import com.summit.stp.shared.constants.RedisConstants;
import com.summit.stp.shared.util.DistributedLockUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostReplyCountScheduler {
    private final CommentsMapper commentsMapper;
    private final RedisTemplate<Object, Object> redisTemplate;
    private final DistributedLockUtil distributedLockUtil;
    private final PostsMapper postsMapper;
    private final TransactionTemplate transactionTemplate;

    @Scheduled(cron = "0 0/10 * * * ?")
    public void updatePostReplyCount() {
        // Tip: 分布式锁，确保多实例并发时只有一个任务能同步回复数
        distributedLockUtil.executeWithLock(RedisConstants.Post.REPLY_CHANGED_LOCK, this::conduct);
    }

    private void conduct() {
        // Tip: 1. 扫描崩溃残留的备份 Key (断点续传)
        Set<Object> leftoverKeys = redisTemplate.keys(RedisConstants.Post.REPLY_CHANGED_RUN_PREFIX + "*");
        if (leftoverKeys != null && !leftoverKeys.isEmpty()) {
            for (Object keyObj : leftoverKeys) {
                if (keyObj instanceof String) {
                    processBackupKey((String) keyObj);
                }
            }
        }

        // Tip: 2. 采用 rename 原子地转移发生变化的帖子 ID 队列到临时备份 Key 中
        String originalKey = RedisConstants.Post.REPLY_CHANGED;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(originalKey))) {
            String backupKey = RedisConstants.Post.REPLY_CHANGED_RUN_PREFIX + UUID.randomUUID();
            try {
                redisTemplate.rename(originalKey, backupKey);
                processBackupKey(backupKey);
            } catch (Exception ignored) {
                // Tip: 重命名失败说明已有其他实例在处理
            }
        }
    }

    private void processBackupKey(String backupKey) {
        Set<Object> changedPostIds = redisTemplate.opsForSet().members(backupKey);
        if (changedPostIds == null || changedPostIds.isEmpty()) {
            redisTemplate.delete(backupKey);
            return;
        }

        // Tip: 3. 构建待更新回复数的帖子 ID 列表
        List<Long> postIds = changedPostIds.stream().map(this::toLong).filter(value -> value != null).toList();
        if (postIds.isEmpty()) {
            redisTemplate.delete(backupKey);
            return;
        }

        try {
            // Tip: 4. 高性能批量查库：使用 SQL IN 一次性统计所有变更帖子的实际回复总数
            List<Map<String, Object>> replyCountRows = commentsMapper.countReplyCountByPostIds(postIds);
            Map<Long, Long> replyCountMap = new HashMap<>(postIds.size());
            if (replyCountRows != null) {
                for (Map<String, Object> row : replyCountRows) {
                    Object postIdValue = row.get("postId");
                    Object replyCountValue = row.get("replyCount");
                    if (postIdValue instanceof Number postIdNumber) {
                        replyCountMap.put(postIdNumber.longValue(), replyCountValue instanceof Number number ? number.longValue() : 0L);
                    }
                }
            }

            // Tip: 5. 开启事务批量更新帖子回复数
            transactionTemplate.executeWithoutResult(status -> {
                for (Long postId : postIds) {
                    LambdaUpdateWrapper<PostsPO> updateWrapper = new LambdaUpdateWrapper<>();
                    updateWrapper.eq(PostsPO::getId, postId)
                            .set(PostsPO::getReplyCount, Math.toIntExact(replyCountMap.getOrDefault(postId, 0L)));
                    postsMapper.update(null, updateWrapper);
                }
            });

            // Tip: 6. 同步成功后，安全删除临时备份 Key
            redisTemplate.delete(backupKey);
            log.info("【定时回复数同步】已批量同步 {} 个帖子回复数至数据库", postIds.size());
        } catch (Exception e) {
            log.error("【定时回复数同步】批量同步失败 postIds={}", postIds, e);
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
