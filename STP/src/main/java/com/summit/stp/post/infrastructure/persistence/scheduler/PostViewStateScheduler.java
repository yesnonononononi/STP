package com.summit.stp.post.infrastructure.persistence.scheduler;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.summit.stp.post.application.service.PostCacheProvider;
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

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostViewStateScheduler {
    private final PostCacheProvider postCacheProvider;
    private final RedisTemplate<Object, Object> redisTemplate;
    private final DistributedLockUtil distributedLockUtil;
    private final PostsMapper postsMapper;
    private final TransactionTemplate transactionTemplate;

    @Scheduled(cron = "30 * * * * ?")
    public void updatePostViewState() {
        distributedLockUtil.executeWithLock(RedisConstants.Post.VIEW_CHANGED_LOCK, this::conduct);
    }

    private void conduct() {
        Set<Object> leftoverKeys = redisTemplate.keys(RedisConstants.Post.VIEW_CHANGED_RUN_PREFIX + "*");
        if (leftoverKeys != null && !leftoverKeys.isEmpty()) {
            for (Object keyObj : leftoverKeys) {
                if (keyObj instanceof String) {
                    processBackupKey((String) keyObj);
                }
            }
        }

        String originalKey = RedisConstants.Post.VIEW_CHANGED;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(originalKey))) {
            String backupKey = RedisConstants.Post.VIEW_CHANGED_RUN_PREFIX + UUID.randomUUID();
            try {
                redisTemplate.rename(originalKey, backupKey);
                processBackupKey(backupKey);
            } catch (Exception ignored) {
            }
        }
    }

    private void processBackupKey(String backupKey) {
        Set<Object> changedPostIds = redisTemplate.opsForSet().members(backupKey);
        if (changedPostIds == null || changedPostIds.isEmpty()) {
            redisTemplate.delete(backupKey);
            return;
        }

        java.util.List<Object> successObjs = new java.util.ArrayList<>();
        java.util.List<Long> validPostIds = new java.util.ArrayList<>();
        java.util.Map<Object, Long> objToIdMap = new java.util.HashMap<>();
        for (Object obj : changedPostIds) {
            Long postId = toLong(obj);
            if (postId == null) {
                redisTemplate.opsForSet().remove(backupKey, obj);
                continue;
            }
            validPostIds.add(postId);
            objToIdMap.put(obj, postId);
        }

        if (!validPostIds.isEmpty()) {
            try {
                java.util.Map<Long, Long> viewCounts = postCacheProvider.getViewCounts(validPostIds);
                transactionTemplate.executeWithoutResult(status -> {
                    for (Map.Entry<Object, Long> entry : objToIdMap.entrySet()) {
                        Object obj = entry.getKey();
                        Long postId = entry.getValue();
                        Long viewCount = viewCounts.getOrDefault(postId, 0L);
                        LambdaUpdateWrapper<PostsPO> updateWrapper = new LambdaUpdateWrapper<>();
                        updateWrapper.eq(PostsPO::getId, postId).set(PostsPO::getViewCount, viewCount);
                        postsMapper.update(null, updateWrapper);
                        successObjs.add(obj);
                    }
                });
            } catch (Exception e) {
                log.error("【定时浏览量同步】批量同步失败 backupKey={}", backupKey, e);
            }
        }

        if (!successObjs.isEmpty()) {
            redisTemplate.opsForSet().remove(backupKey, successObjs.toArray());
        }

        Long size = redisTemplate.opsForSet().size(backupKey);
        if (size == null || size == 0) {
            redisTemplate.delete(backupKey);
        }
    }

    private Long toLong(Object obj) {
        if (obj instanceof Long value) return value;
        if (obj instanceof Integer value) return value.longValue();
        if (obj instanceof String value) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }
}
