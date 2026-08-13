package com.summit.stp.post.infrastructure.persistence.scheduler;

import com.summit.stp.common.application.domain.event.UserLikedChangeEvent;
import com.summit.stp.common.application.service.queue.QueueSender;
import com.summit.stp.common.constants.MqConstants;
import com.summit.stp.common.util.DistributedLockUtil;
import com.summit.stp.post.application.service.PostCacheProvider;
import com.summit.stp.post.domain.model.Post;
import com.summit.stp.post.domain.repository.PostCollectRepository;
import com.summit.stp.post.domain.repository.PostLikeRepository;
import com.summit.stp.common.constants.CacheFieldConstants;
import com.summit.stp.post.infrastructure.constants.PostConstants;
import com.summit.stp.post.infrastructure.persistence.repoImpl.PostRepositoryImpl;

import com.summit.stp.post.infrastructure.persistence.po.PostCollectPO;
import com.summit.stp.post.infrastructure.persistence.po.PostLikePO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final PostCacheProvider postCacheProvider;
    private final PostLikeRepository postLikeRepository;
    private final PostCollectRepository postCollectRepository;
    private final DistributedLockUtil distributedLockUtil;
    private final QueueSender queueSender;
    private final TransactionTemplate transactionTemplate;
    private final PostRepositoryImpl postRepositoryImpl;




    @Scheduled(cron = "0 0/3 * * * ?")
    public void syncAll() {
        log.info("【帖子】统一同步定时任务开始执行");
        distributedLockUtil.executeWithLock(PostConstants.Cache.CHANGED_LOCK, this::conduct);
    }



    /**
     * 对一批变更帖子执行完整同步
     */
    private void executeSync(List<Long> idList) {
        // 1. 预热缺失缓存
        postCacheProvider.loadCache(idList);

        // 2. 点赞映射增量同步
        Map<Long, Integer> likeDeltas = syncLikes(idList);

        // 3. 收藏映射增量同步
        syncCollects(idList);

        // 4. 事务内合并更新计数字段 + 发送通知
        batchUpdateCounts(idList, likeDeltas);

    }

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



    /**
     * 事务内合并更新 posts 表的 like_count、collect_count、view_count、reply_count，
     * 并发送点赞增量事件通知
     */
    private void batchUpdateCounts(List<Long> postIdList, Map<Long, Integer> likeDeltas) {
        Map<Long, Double> scoresMap = new HashMap<>();
        //0, 从缓存读取权威最新计数（包含用户增减点赞与收藏的真实 Hash 计数）
        Map<Long, Map<String, Long>> countMap = postCacheProvider.getLikeAndCollectCount(postIdList);
        Map<Long, Long> viewCounts = postCacheProvider.getViewCounts(postIdList);
        Map<Long, Long> replyCounts = postCacheProvider.getReplyCounts(postIdList);

        //1, 批量查询帖子（用于获取创建时间、创建者 ID）
        Map<Long, Post> postPoMap = batchSelectPost(postIdList);

        //2, 事务更新帖子db和缓存
        transactionTemplate.executeWithoutResult(status -> {
            List<Post> willCachePosts = new ArrayList<>();
            for (Long postId : postIdList) {
                Map<String, Long> postCounts = countMap.getOrDefault(postId, Collections.emptyMap());
                long likeCount = postCounts.getOrDefault(CacheFieldConstants.LIKE_COUNT, 0L);
                long collectCount = postCounts.getOrDefault(CacheFieldConstants.COLLECT_COUNT, 0L);
                long viewCount = viewCounts.getOrDefault(postId, 0L);
                long replyCount = replyCounts.getOrDefault(postId, 0L);
                Long creatorId = null;
                Post post = postPoMap.get(postId);
                Integer delta = likeDeltas.get(postId);

                //2.1 更新帖子db
                if (post != null) {
                    post.updateMeta(likeCount, viewCount, collectCount, replyCount);
                    double hotScore = post.calculateHotScore();
                    scoresMap.put(postId, hotScore);
                    postRepositoryImpl.update(post);
                    creatorId = post.getCreatorId();
                    willCachePosts.add(post);
                }


                //2.2 点赞增量事件通知
                if (delta != null && creatorId != null) {
                    queueSender.send(MqConstants.User.EXCHANGE, MqConstants.User.ROUTING_KEY_LIKED, UserLikedChangeEvent.builder()
                            .userId(creatorId)
                            .likedDelta(delta)
                            .build());
                }
            }
            //2.3 同步写回 Redis detail 缓存
            postCacheProvider.batchCachePostDetail(willCachePosts);
        });

        //3,  增量同步计算后的热度得分到 Redis 热门 ZSet
        if (!scoresMap.isEmpty()) {
            try {
                postCacheProvider.addToHotZSet(scoresMap);
            } catch (Exception e) {
                log.error("【帖子】批量同步热度分数至 Redis 异常", e);
            }
        }


        log.info("【帖子】已合并同步并更新热度 {} 个帖子状态至数据库", postIdList.size());
    }



    /**
     * 处理变更集合：先清理残留备份 → 再 rename 当前集合并处理
     */
    private void conduct() {
        // 1. 处理残留的备份 Key
        Set<String> leftoverKeys = postCacheProvider.scanChangedRunKeys();
        for (String key : leftoverKeys) {
            processBackupKey(key);
        }

        // 2. rename 当前集合并处理
        if (postCacheProvider.hasChangedKey()) {
            String backupKey = PostConstants.Cache.CHANGED_RUN_PREFIX + UUID.randomUUID();
            if (postCacheProvider.renameChangedKey(backupKey)) {
                processBackupKey(backupKey);
            }
        }
    }

    /**
     * 处理单个备份 Key：提取变更 ID → 执行同步 → 清理
     */
    private void processBackupKey(String backupKey) {
        Set<Long> changedPostIds = postCacheProvider.getBackupPostIds(backupKey);
        if (changedPostIds == null || changedPostIds.isEmpty()) {
            postCacheProvider.deleteBackupKey(backupKey);
            return;
        }

        // 获取最多1500条,防止大批量更新影响数据库性能
        List<Long> rawObjs = new ArrayList<>(changedPostIds);

        int allNum = rawObjs.size();
        int attemptNum = Math.min(allNum, 1500);

        List<Long> idList = rawObjs.subList(0, attemptNum);

        if (!idList.isEmpty()) {
            try {
                executeSync(idList);
                postCacheProvider.removeBackupPostIds(backupKey, idList);
            } catch (Exception e) {
                log.error("【帖子】同步任务执行异常 backupKey={}", backupKey, e);
            }
        }
        if (attemptNum == allNum) {
            postCacheProvider.deleteBackupKey(backupKey);
        }
    }


    /**
     * 批量获取帖子实体从db
     * @param postIdList 帖子Id集合
     * @return 帖子实体Map，key为帖子ID
     */
    private  Map<Long, Post> batchSelectPost(Collection<Long> postIdList){
        List<Post> postsList =  postRepositoryImpl.findByIds((List<Long>) postIdList);
        return postsList == null ? Collections.emptyMap() :
                postsList.stream().collect(Collectors.toMap(Post::getId, Function.identity()));
    }
}

