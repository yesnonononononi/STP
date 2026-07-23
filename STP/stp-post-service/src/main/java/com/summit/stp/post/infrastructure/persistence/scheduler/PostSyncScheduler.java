package com.summit.stp.post.infrastructure.persistence.scheduler;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.summit.stp.post.application.service.PostCacheProvider;
import com.summit.stp.shared.domain.event.UserLikedChangeEvent;
import com.summit.stp.post.domain.model.Post;
import com.summit.stp.post.domain.repository.PostCollectRepository;
import com.summit.stp.post.domain.repository.PostLikeRepository;
import com.summit.stp.post.infrastructure.persistence.mapper.PostsMapper;
import com.summit.stp.post.infrastructure.persistence.po.PostCollectPO;
import com.summit.stp.post.infrastructure.persistence.po.PostLikePO;
import com.summit.stp.post.infrastructure.persistence.po.PostsPO;
import com.summit.stp.shared.constants.MqConstants;
import com.summit.stp.post.infrastructure.constants.PostConstants;
import com.summit.stp.shared.util.DistributedLockUtil;
import com.summit.stp.shared.service.queue.QueueSender;
import com.summit.stp.rank_board.application.service.RankCacheProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 帖子状态统一同步调度器
 * <p>
 * 合并原有的 PostStateScheduler（点赞/收藏）、PostViewStateScheduler（浏览数）、
 * PostReplyCountScheduler（回复数）三个独立定时任务，在一次调度中完成：
 * <ol>
 *   <li>点赞/收藏映射关系的增量 diffSync（Redis Set ↔ DB post_likes/post_collects）</li>
 *   <li>帖子表 posts 四个计数字段的事务合并更新（like_count, collect_count, view_count, reply_count）</li>
 * </ol>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostSyncScheduler {

    private final RedisTemplate<String, Object> redisTemplate;
    private final PostCacheProvider postCacheProvider;
    private final PostLikeRepository postLikeRepository;
    private final PostCollectRepository postCollectRepository;
    private final PostsMapper postsMapper;
    private final DistributedLockUtil distributedLockUtil;
    private final QueueSender queueSender;
    private final TransactionTemplate transactionTemplate;
    private final RankCacheProvider rankCacheProvider;


    // ======================== 定时入口 ========================

    @Scheduled(cron = "0 0/3 * * * ?")
    public void syncAll() {
        log.info("【帖子】统一同步定时任务开始执行");
        distributedLockUtil.executeWithLock(PostConstants.Cache.CHANGED_LOCK, this::conduct);
    }

    // ======================== 编排流程 ========================

    /**
     * 对一批变更帖子执行完整同步
     */
    private void executeSync(List<Long> idList) {
        // 1. 预热缺失缓存
        postCacheProvider.loadCache(idList);

        int batchCommitSize = 500;
        for (int i = 0; i < idList.size(); i += batchCommitSize) {
            List<Long> batch = idList.subList(i, Math.min(i + batchCommitSize, idList.size()));

            // 2. 点赞映射增量同步
            Map<Long, Integer> likeDeltas = syncLikes(batch);

            // 3. 收藏映射增量同步
            syncCollects(batch);

            // 4. 事务内合并更新计数字段 + 发送通知
            batchUpdateCounts(batch, likeDeltas);
        }
    }

    // ======================== 点赞/收藏 diffSync ========================

    /**
     * 点赞差集同步，返回各帖子的点赞增量 delta（用于事件通知）
     */
    private Map<Long, Integer> syncLikes(List<Long> postIdList) {
        return diffSync(postIdList,
                postCacheProvider::batchGetLikeUserIds,
                postLikeRepository::findUserIdsByPostIds,
                (postId, uid) -> PostLikePO.builder().postId(postId).userId(uid).build(),
                postLikeRepository::batchSave,
                postLikeRepository::batchDelete
        );
    }

    /**
     * 收藏差集同步
     */
    private void syncCollects(List<Long> postIdList) {
        diffSync(postIdList,
                postCacheProvider::batchGetCollectUserIds,
                postCollectRepository::findUserIdsByPostIds,
                (postId, uid) -> PostCollectPO.builder().postId(postId).userId(uid).build(),
                postCollectRepository::batchSave,
                postCollectRepository::batchDelete
        );
    }

    /**
     * 通用 Redis ↔ DB 差集同步模板
     *
     * @return 各 postId 的增量 delta（added - removed）
     */
    private <PO> Map<Long, Integer> diffSync(
            List<Long> postIdList,
            Function<List<Long>, Map<Long, Set<Long>>> redisReader,
            Function<List<Long>, Map<Long, List<Long>>> dbReader,
            BiFunction<Long, Long, PO> poBuilder,
            Consumer<List<PO>> batchSaver,
            Consumer<List<Long[]>> batchRemover
    ) {
        if (postIdList == null || postIdList.isEmpty()) return Collections.emptyMap();

        Map<Long, Set<Long>> redisMap = redisReader.apply(postIdList);
        Map<Long, List<Long>> dbMap = dbReader.apply(postIdList);

        List<PO> toAddList = new ArrayList<>();
        List<Long[]> toRemoveList = new ArrayList<>();
        Map<Long, Integer> deltas = new HashMap<>();

        for (Long postId : postIdList) {
            Set<Long> cacheSet = redisMap.getOrDefault(postId, Collections.emptySet());
            List<Long> dbList = dbMap.getOrDefault(postId, Collections.emptyList());

            int added = 0, removed = 0;
            for (Long uid : dbList) {
                if (!cacheSet.contains(uid)) {
                    toRemoveList.add(new Long[]{postId, uid});
                    removed++;
                }
            }
            for (Long uid : cacheSet) {
                if (!dbList.contains(uid)) {
                    toAddList.add(poBuilder.apply(postId, uid));
                    added++;
                }
            }
            if (added - removed != 0) {
                deltas.put(postId, added - removed);
            }
        }

        if (!toAddList.isEmpty()) batchSaver.accept(toAddList);
        if (!toRemoveList.isEmpty()) batchRemover.accept(toRemoveList);

        return deltas;
    }

    // ======================== 事务合并更新计数 ========================

    /**
     * 事务内合并更新 posts 表的 like_count、collect_count、view_count、reply_count，
     * 并发送点赞增量事件通知
     */
    private void batchUpdateCounts(List<Long> postIdList, Map<Long, Integer> likeDeltas) {
        // 从缓存读取最新计数
        Map<Long, Set<Long>> likeMap = postCacheProvider.batchGetLikeUserIds(postIdList);
        Map<Long, Set<Long>> collectMap = postCacheProvider.batchGetCollectUserIds(postIdList);
        Map<Long, Long> viewCounts = postCacheProvider.getViewCounts(postIdList);
        Map<Long, Long> replyCounts = postCacheProvider.getReplyCounts(postIdList);

        // 批量查询帖子（用于获取创建时间、创建者 ID）
        List<PostsPO> postsList = postsMapper.selectByIds(postIdList);
        Map<Long, PostsPO> postPoMap = postsList == null ? Collections.emptyMap() :
                postsList.stream().collect(Collectors.toMap(PostsPO::getId, Function.identity()));

        Map<Long, Double> scoresMap = new HashMap<>();

        transactionTemplate.executeWithoutResult(status -> {
            for (Long postId : postIdList) {
                long likeCount = likeMap.getOrDefault(postId, Collections.emptySet()).size();
                long collectCount = collectMap.getOrDefault(postId, Collections.emptySet()).size();
                long viewCount = viewCounts.getOrDefault(postId, 0L);
                long replyCount = replyCounts.getOrDefault(postId, 0L);

                PostsPO po = postPoMap.get(postId);
                double hotScore = 0.0;
                if (po != null) {
                    Post postDomain = Post.builder()
                            .id(postId)
                            .createTime(po.getCreateTime())
                            .likeCount(likeCount)
                            .replyCount(replyCount)
                            .viewCount(viewCount)
                            .collectCount(collectCount)
                            .build();
                    hotScore = postDomain.calculateHotScore();
                    scoresMap.put(postId, hotScore);
                }

                LambdaUpdateWrapper<PostsPO> wrapper = new LambdaUpdateWrapper<>();
                wrapper.eq(PostsPO::getId, postId)
                        .set(PostsPO::getLikeCount, likeCount)
                        .set(PostsPO::getCollectCount, collectCount)
                        .set(PostsPO::getViewCount, viewCount)
                        .set(PostsPO::getReplyCount, replyCount)
                        .set(PostsPO::getHotScore, (long) Math.ceil(hotScore));
                postsMapper.update(null, wrapper);

                // 同步写回 Redis detail 缓存
                String postKey = PostConstants.Cache.DETAIL_PREFIX + postId;
                if (Boolean.TRUE.equals(redisTemplate.hasKey(postKey))) {
                    Map<String, Object> fields = new HashMap<>();
                    fields.put(com.summit.stp.shared.constants.CacheFieldConstants.LIKE_COUNT, likeCount);
                    fields.put(com.summit.stp.shared.constants.CacheFieldConstants.COLLECT_COUNT, collectCount);
                    fields.put(com.summit.stp.shared.constants.CacheFieldConstants.VIEW_COUNT, viewCount);
                    fields.put(com.summit.stp.shared.constants.CacheFieldConstants.REPLY_COUNT, replyCount);
                    fields.put("hotScore", (long) Math.ceil(hotScore));
                    redisTemplate.opsForHash().putAll(postKey, fields);
                }

                // 点赞增量事件通知
                Integer delta = likeDeltas.get(postId);
                Long creatorId = po != null ? po.getCreatorId() : null;
                if (delta != null && creatorId != null) {
                    queueSender.send(MqConstants.User.EXCHANGE, MqConstants.User.ROUTING_KEY_LIKED, UserLikedChangeEvent.builder()
                            .userId(creatorId)
                            .likedDelta(delta)
                            .build());
                }
            }
        });

        // 增量同步计算后的热度得分到 Redis 热门 ZSet 和排行榜 ZSet
        if (!scoresMap.isEmpty()) {
            try {
                // 1. 同步到排行榜系统的热门 ZSet (ZSET: PostConstants.Cache.POST_HOT_ZSET)
                rankCacheProvider.cachePostsScore(scoresMap);

                // 2. 同步到主页游标查询的热门 ZSet (ZSET: PostConstants.Cache.QUERY_HOT)
                scoresMap.forEach(postCacheProvider::addToHotZSet);
            } catch (Exception e) {
                log.error("【帖子】批量同步热度分数至 Redis 异常", e);
            }
        }

        log.info("【帖子】已合并同步并更新热度 {} 个帖子状态至数据库", postIdList.size());
    }

    // ======================== 变更集合处理 ========================

    /**
     * 处理变更集合：先清理残留备份 → 再 rename 当前集合并处理
     */
    private void conduct() {
        // 1. 处理残留的备份 Key
        Set<String> leftoverKeys = scanKeys(PostConstants.Cache.CHANGED_RUN_PREFIX + "*");
        for (String key : leftoverKeys) {
            processBackupKey(key);
        }

        // 2. rename 当前集合并处理
        if (Boolean.TRUE.equals(redisTemplate.hasKey(PostConstants.Cache.CHANGED))) {
            String backupKey = PostConstants.Cache.CHANGED_RUN_PREFIX + UUID.randomUUID();
            try {
                redisTemplate.rename(PostConstants.Cache.CHANGED, backupKey);
                processBackupKey(backupKey);
            } catch (Exception ignored) {
                // 并发 rename 失败说明已有其他实例在处理
            }
        }
    }

    /**
     * 处理单个备份 Key：提取变更 ID → 执行同步 → 清理
     */
    private void processBackupKey(String backupKey) {
        Set<Object> changedPostIds = redisTemplate.opsForSet().members(backupKey);
        if (changedPostIds == null || changedPostIds.isEmpty()) {
            redisTemplate.delete(backupKey);
            return;
        }

        List<Object> rawObjs = new ArrayList<>(changedPostIds);
        List<Object> toProcessObjs = rawObjs.subList(0, Math.min(rawObjs.size(), 1500));

        List<Long> idList = new ArrayList<>();
        List<Object> validObjs = new ArrayList<>();
        for (Object obj : toProcessObjs) {
            Long id = toLong(obj);
            if (id != null) {
                idList.add(id);
                validObjs.add(obj);
            } else {
                redisTemplate.opsForSet().remove(backupKey, obj);
            }
        }

        if (!idList.isEmpty()) {
            try {
                executeSync(idList);
                redisTemplate.opsForSet().remove(backupKey, validObjs.toArray());
            } catch (Exception e) {
                log.error("【帖子】同步任务执行异常 backupKey={}", backupKey, e);
            }
        }

        Long size = redisTemplate.opsForSet().size(backupKey);
        if (size == null || size == 0) {
            redisTemplate.delete(backupKey);
        }
    }

    // ======================== 工具方法 ========================

    private Set<String> scanKeys(String pattern) {
        Set<String> keys = new HashSet<>();
        try {
            redisTemplate.execute((RedisCallback<Void>) connection -> {
                Cursor<byte[]> cursor = connection.keyCommands().scan(
                        ScanOptions.scanOptions().match(pattern).count(1000).build()
                );
                while (cursor.hasNext()) {
                    Object deserialized = redisTemplate.getKeySerializer().deserialize(cursor.next());
                    if (deserialized instanceof String str) {
                        keys.add(str);
                    }
                }
                return null;
            });
        } catch (Exception e) {
            log.error("【帖子】Scan Redis 执行失败, pattern={}", pattern, e);
        }
        return keys;
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
