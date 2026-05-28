package com.summit.stp.post.infrastructure.persistence.scheduler;

import com.summit.stp.post.application.service.PostCacheProvider;
import com.summit.stp.post.domain.repository.PostCollectRepository;
import com.summit.stp.post.domain.repository.PostLikeRepository;
import com.summit.stp.post.infrastructure.persistence.po.PostCollectPO;
import com.summit.stp.post.infrastructure.persistence.po.PostLikePO;
import com.summit.stp.shared.util.DistributedLockUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;

@Component
public class PostStateScheduler {
    private final PostCacheProvider postCacheProvider;
    private final PostLikeRepository postLikeRepository;
    private final PostCollectRepository postCollectRepository;
    private final RedisTemplate<Object, Object> redisTemplate;
    private final DistributedLockUtil distributedLockUtil;

    public PostStateScheduler(PostCacheProvider postCacheProvider,
                              PostLikeRepository postLikeRepository,
                              PostCollectRepository postCollectRepository,
                              RedisTemplate<Object, Object> redisTemplate, DistributedLockUtil distributedLockUtil) {
        this.postCacheProvider = postCacheProvider;
        this.postLikeRepository = postLikeRepository;
        this.postCollectRepository = postCollectRepository;
        this.redisTemplate = redisTemplate;
        this.distributedLockUtil = distributedLockUtil;
    }

    @Scheduled(cron = "0 * * * * ?")
    public void updatePostState() {
        final String LOCK_KEY = "post:changed:lock";
        distributedLockUtil.executeWithLock(LOCK_KEY, this::conduct);
    }

    private void conduct() {
        // 1. 扫描并优先处理由于系统崩溃遗留下来的备份 Key（断点续传）
        Set<Object> leftoverKeys = redisTemplate.keys("post:changed:run:*");
        if (leftoverKeys != null && !leftoverKeys.isEmpty()) {
            for (Object keyObj : leftoverKeys) {
                if (keyObj instanceof String) {
                    processBackupKey((String) keyObj);
                }
            }
        }

        // 2. 处理当前最新的变更
        String originalKey = "post:changed";
        Boolean hasKey = redisTemplate.hasKey(originalKey);
        if (Boolean.TRUE.equals(hasKey)) {
            String backupKey = "post:changed:run:" + UUID.randomUUID().toString();
            try {
                redisTemplate.rename(originalKey, backupKey);
                processBackupKey(backupKey);
            } catch (Exception e) {
                // rename 失败通常表示 originalKey 在这期间被其他实例处理了
            }
        }
    }

    private void processBackupKey(String backupKey) {
        Set<Object> changedPostIds = redisTemplate.opsForSet().members(backupKey);
        if (changedPostIds == null || changedPostIds.isEmpty()) {
            redisTemplate.delete(backupKey);
            return;
        }

        // 类型批量转换并同步落库
        for (Object obj : changedPostIds) {
            Long postId = null;
            if (obj instanceof Long) {
                postId = (Long) obj;
            } else if (obj instanceof Integer) {
                postId = ((Integer) obj).longValue();
            } else if (obj instanceof String) {
                try {
                    postId = Long.parseLong((String) obj);
                } catch (NumberFormatException ignored) {}
            }
            if (postId == null) {
                redisTemplate.opsForSet().remove(backupKey, obj);
                continue;
            }

            try {
                // 3. 强制预热防御：确保同步前 Redis 中的 ZSet 已经完整构建，防止 ZSet 被驱逐导致空集删除数据库中已有数据
                postCacheProvider.loadCache(postId);

                // 4. 同步点赞与收藏关系入库
                syncLikes(postId);
                syncCollects(postId);

                // 5. 单条处理成功后，才从备份 Key 中移除，实现元素级断点保存
                redisTemplate.opsForSet().remove(backupKey, obj);
            } catch (Exception ignored) {
                // 异常隔离：单个帖子同步失败不阻塞其他帖子同步，且保留在备份 Key 中下次重试
            }
        }

        // 6. 若备份 Key 中所有元素都已成功同步并清空，则彻底删除该 Key
        Long size = redisTemplate.opsForSet().size(backupKey);
        if (size == null || size == 0) {
            redisTemplate.delete(backupKey);
        }
    }

    private void syncLikes(Long postId) {
        Set<Long> redisLikes = postCacheProvider.getLikeUserIds(postId);
        List<Long> dbLikesList = postLikeRepository.findUserIdsByPostId(postId);
        Set<Long> dbLikes = dbLikesList != null ? new HashSet<>(dbLikesList) : new HashSet<>();

        //同步点赞
        Set<Long> toAdd = new HashSet<>(redisLikes);
        toAdd.removeAll(dbLikes);
        for (Long userId : toAdd) {
            PostLikePO po = new PostLikePO();
            po.setPostId(postId);
            po.setUserId(userId);
            try {
                postLikeRepository.save(po);
            } catch (Exception ignored) {
            }
        }

        // 取消点赞
        Set<Long> toRemove = new HashSet<>(dbLikes);
        toRemove.removeAll(redisLikes);
        for (Long userId : toRemove) {
            try {
                postLikeRepository.delete(postId, userId);
            } catch (Exception ignored) {

            }
        }

    }

    private void syncCollects(Long postId) {
        Set<Long> redisCollects = postCacheProvider.getCollectUserIds(postId);
        List<Long> dbCollectsList = postCollectRepository.findUserIdsByPostId(postId);
        Set<Long> dbCollects = dbCollectsList != null ? new HashSet<>(dbCollectsList) : new HashSet<>();

     //同步收藏操作
        Set<Long> toAdd = new HashSet<>(redisCollects);
        toAdd.removeAll(dbCollects);
        for (Long userId : toAdd) {
            PostCollectPO po = new PostCollectPO();
            po.setPostId(postId);
            po.setUserId(userId);
            try {
                postCollectRepository.save(po);
            } catch (Exception ignored) {

            }
        }

       //同步取消收藏操作
        Set<Long> toRemove = new HashSet<>(dbCollects);
        toRemove.removeAll(redisCollects); //注意,这里redisCollects如果为空,那么toRemove将全都是db的原有数据,会导致数据库点赞数据的全部清空!所以会先LoadCache一遍
        for (Long userId : toRemove) {
            try {
                postCollectRepository.delete(postId, userId);
            } catch (Exception ignored) {

            }
        }

    }



}
