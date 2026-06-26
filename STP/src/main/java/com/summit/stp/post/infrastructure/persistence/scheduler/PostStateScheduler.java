package com.summit.stp.post.infrastructure.persistence.scheduler;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.summit.stp.post.application.service.PostCacheProvider;
import com.summit.stp.post.domain.event.UserLikedChangeEvent;
import com.summit.stp.post.domain.model.Post;
import com.summit.stp.post.domain.repository.PostCollectRepository;
import com.summit.stp.post.domain.repository.PostLikeRepository;
import com.summit.stp.post.domain.repository.PostRepository;
import com.summit.stp.post.infrastructure.persistence.mapper.PostsMapper;
import com.summit.stp.post.infrastructure.persistence.po.PostCollectPO;
import com.summit.stp.post.infrastructure.persistence.po.PostLikePO;
import com.summit.stp.post.infrastructure.persistence.po.PostsPO;
import com.summit.stp.shared.constants.RedisConstants;
import com.summit.stp.shared.util.DistributedLockUtil;
import com.summit.stp.user.application.service.UserMessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
public class PostStateScheduler {
    private final PostCacheProvider postCacheProvider;
    private final PostLikeRepository postLikeRepository;
    private final PostCollectRepository postCollectRepository;
    private final RedisTemplate<Object, Object> redisTemplate;
    private final DistributedLockUtil distributedLockUtil;
    private final PostRepository postRepository;
    private final UserMessageSender userMessageSender;
    private final PostsMapper postsMapper;

    public PostStateScheduler(PostCacheProvider postCacheProvider,
                              PostLikeRepository postLikeRepository,
                              PostCollectRepository postCollectRepository,
                              RedisTemplate<Object, Object> redisTemplate,
                              DistributedLockUtil distributedLockUtil,
                              PostRepository postRepository,
                              UserMessageSender userMessageSender,
                              PostsMapper postsMapper) {
        this.postCacheProvider = postCacheProvider;
        this.postLikeRepository = postLikeRepository;
        this.postCollectRepository = postCollectRepository;
        this.redisTemplate = redisTemplate;
        this.distributedLockUtil = distributedLockUtil;
        this.postRepository = postRepository;
        this.userMessageSender = userMessageSender;
        this.postsMapper = postsMapper;
    }

    @Scheduled(cron = "0 * * * * ?")
    public void updatePostState() {
        log.info("【定时帖子状态同步】");
        distributedLockUtil.executeWithLock(RedisConstants.Post.LIKE_COLLECT_CHANGED_LOCK, this::conduct);
    }

    private void conduct() {
        // Tip: 1. 处理系统异常退出残留的备份 Key (断点续传机制)
        Set<Object> leftoverKeys = redisTemplate.keys(RedisConstants.Post.LIKE_COLLECT_CHANGED_RUN_PREFIX + "*");
        if (leftoverKeys != null && !leftoverKeys.isEmpty()) {
            for (Object keyObj : leftoverKeys) {
                if (keyObj instanceof String) {
                    processBackupKey((String) keyObj);
                }
            }
        }

        // Tip: 2. 正常将待更新队列 rename 至 UUID 备份 Key，以实现安全增量更新
        String originalKey = RedisConstants.Post.LIKE_COLLECT_CHANGED;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(originalKey))) {
            String backupKey = RedisConstants.Post.LIKE_COLLECT_CHANGED_RUN_PREFIX + UUID.randomUUID();
            try {
                redisTemplate.rename(originalKey, backupKey);
                processBackupKey(backupKey);
            } catch (Exception ignored) {
                // Tip: 重命名失败说明已有其他实例在并行执行
            }
        }
    }

    private void processBackupKey(String backupKey) {
        Set<Object> changedPostIds = redisTemplate.opsForSet().members(backupKey);
        if (changedPostIds == null || changedPostIds.isEmpty()) {
            redisTemplate.delete(backupKey);
            return;
        }

        // Tip: 3. 逐个同步发生改变的帖子状态
        for (Object obj : changedPostIds) {
            Long postId = toLong(obj);
            if (postId == null) {
                redisTemplate.opsForSet().remove(backupKey, obj);
                continue;
            }
            try {
                // 3. 强制预热防御：确保同步前 Redis 中的 ZSet 已经完整构建，防止 ZSet 被驱逐导致空集删除数据库中已有数据
                postCacheProvider.loadCache(postId);

                // Tip: 3.2 同步点赞与收藏关系到 MySQL 数据库中
                syncLikes(postId);
                syncCollects(postId);

                // Tip: 3.3 同步成功后剔除当前元素，提供细粒度的断点恢复
                redisTemplate.opsForSet().remove(backupKey, obj);
            } catch (Exception e) {
                log.error("【定时帖子状态同步】同步帖子失败: postId={}", postId, e);
            }
        }

        // Tip: 4. 本轮全部更新完后，删除临时备份 Key
        Long size = redisTemplate.opsForSet().size(backupKey);
        if (size == null || size == 0) {
            redisTemplate.delete(backupKey);
        }
    }

    private void syncLikes(Long postId) {
        // Tip: 批量获取 Redis 与数据库中该帖子的点赞用户列表
        Set<Long> redisLikes = postCacheProvider.getLikeUserIds(postId);
        List<Long> dbLikesList = postLikeRepository.findUserIdsByPostId(postId);
        Set<Long> dbLikes = dbLikesList != null ? new HashSet<>(dbLikesList) : new HashSet<>();

        // Tip: 求差集以计算需要增量写入的用户 (Redis 中有，DB 中无)
        Set<Long> toAdd = new HashSet<>(redisLikes);
        toAdd.removeAll(dbLikes);
        int addSuccess = 0;
        for (Long userId : toAdd) {
            PostLikePO po = new PostLikePO();
            po.setPostId(postId);
            po.setUserId(userId);
            try {
                postLikeRepository.save(po);
                addSuccess++;
            } catch (Exception ignored) {}
        }

        // Tip: 求差集以计算需要从 DB 中物理删除的取消点赞记录 (DB 中有，Redis 中无)
        Set<Long> toRemove = new HashSet<>(dbLikes);
        toRemove.removeAll(redisLikes);
        int removeSuccess = 0;
        for (Long userId : toRemove) {
            try {
                postLikeRepository.delete(postId, userId);
                removeSuccess++;
            } catch (Exception ignored) {}
        }

        // Tip: 如果点赞总数发生了增减变化，通过 MQ 消息队列异步通知对应的发帖作者
        int delta = addSuccess - removeSuccess;
        if (delta != 0) {
            Post post = postRepository.findById(postId);
            if (post != null) {
                UserLikedChangeEvent event = UserLikedChangeEvent.builder()
                        .userId(post.getCreatorId())
                        .likedDelta(delta)
                        .build();
                try {
                    userMessageSender.sendLikedUpdate(event);
                } catch (Exception e) {
                    log.error("【定时点赞同步】发送MQ通知失败: postId={}, delta={}", postId, delta, e);
                }
            }
        }
    }

    private void syncCollects(Long postId) {
        // Tip: 批量获取 Redis 与数据库中该帖子的收藏用户列表
        Set<Long> redisCollects = postCacheProvider.getCollectUserIds(postId);
        List<Long> dbCollectsList = postCollectRepository.findUserIdsByPostId(postId);
        Set<Long> dbCollects = dbCollectsList != null ? new HashSet<>(dbCollectsList) : new HashSet<>();

        // Tip: 增量添加收藏记录
        Set<Long> toAdd = new HashSet<>(redisCollects);
        toAdd.removeAll(dbCollects);
        for (Long userId : toAdd) {
            PostCollectPO po = new PostCollectPO();
            po.setPostId(postId);
            po.setUserId(userId);
            try {
                postCollectRepository.save(po);
            } catch (Exception ignored) {}
        }

        // Tip: 增量删除取消收藏记录
        Set<Long> toRemove = new HashSet<>(dbCollects);
        toRemove.removeAll(redisCollects);
        for (Long userId : toRemove) {
            try {
                postCollectRepository.delete(postId, userId);
            } catch (Exception ignored) {}
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
