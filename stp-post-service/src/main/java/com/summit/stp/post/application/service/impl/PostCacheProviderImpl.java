package com.summit.stp.post.application.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.summit.stp.common.constants.CacheFieldConstants;
import com.summit.stp.common.util.DistributedLockUtil;
import com.summit.stp.post.application.service.PostCacheProvider;
import com.summit.stp.post.application.service.impl.cache.*;
import com.summit.stp.post.application.vo.PostVO;
import com.summit.stp.post.domain.model.Post;
import com.summit.stp.post.domain.repository.PostCollectRepository;
import com.summit.stp.post.domain.repository.PostLikeRepository;
import com.summit.stp.post.infrastructure.constants.PostConstants;
import com.summit.stp.post.infrastructure.persistence.mapper.PostsMapper;
import com.summit.stp.post.infrastructure.persistence.po.PostsPO;
import com.summit.stp.tag.domain.model.PostTag;
import com.summit.stp.tag.domain.repository.PostTagRelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 帖子缓存服务实现类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostCacheProviderImpl implements PostCacheProvider {

    private final PostInteractionCacheOps interactionCacheOps;
    private final PostCounterCacheOps counterCacheOps;
    private final PostContentCacheOps contentCacheOps;
    private final PostQueryZSetCacheOps queryZSetCacheOps;
    private final PostLikeRepository postLikeRepository;
    private final PostCollectRepository postCollectRepository;
    private final PostTagRelRepository postTagRelRepository;
    private final PostsMapper postsMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final DistributedLockUtil distributedLockUtil;

    // ======================== 互动（点赞/收藏） ========================

    @Override
    public boolean like(Long postId, Long userId) {
        loadCache(postId);
        boolean toggle = interactionCacheOps.toggle(postId, userId, InteractionType.LIKE);
        markChanged(postId);
        return toggle;
    }

    @Override
    public boolean collect(Long postId, Long userId) {
        loadCache(postId);
        boolean toggle = interactionCacheOps.toggle(postId, userId, InteractionType.COLLECT);
        markChanged(postId);
        return toggle;
    }

    @Override
    public boolean isLiked(Long postId, Long userId) {
        return interactionCacheOps.isMember(postId, userId, InteractionType.LIKE);
    }

    @Override
    public boolean isCollected(Long postId, Long userId) {
        return interactionCacheOps.isMember(postId, userId, InteractionType.COLLECT);
    }

    @Override
    public Map<Long, Set<Long>> batchGetLikeUserIds(List<Long> postIdList) {
        return interactionCacheOps.batchGetUserIds(postIdList, InteractionType.LIKE);
    }

    @Override
    public Map<Long, Set<Long>> batchGetCollectUserIds(List<Long> postIdList) {
        return interactionCacheOps.batchGetUserIds(postIdList, InteractionType.COLLECT);
    }

    // ======================== 变更标记 ========================

    @Override
    public void markChanged(Object postId) {
        interactionCacheOps.markChanged(postId, PostConstants.Cache.CHANGED);
    }

    // ======================== 浏览数 / 回复数 ========================

    @Override
    public void incrViewCount(Long postId) {
        if (postId == null) return;
        loadCache(postId);
        counterCacheOps.incrViewCount(postId);
        markChanged(postId);
    }

    @Override
    public void incrReplyCount(Long postId) {
        if (postId == null) return;
        loadCache(postId);
        counterCacheOps.incrReplyCount(postId);
        markChanged(postId);
    }

    @Override
    public void decrReplyCount(Long postId) {
        if (postId == null) return;
        loadCache(postId);
        counterCacheOps.decrReplyCount(postId);
        markChanged(postId);
    }

    @Override
    public Map<Long, Long> getViewCounts(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) return Collections.emptyMap();
        loadCache(postIds);
        return counterCacheOps.batchGetCounts(postIds, CacheFieldConstants.VIEW_COUNT);
    }

    @Override
    public Map<Long, Long> getReplyCounts(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) return Collections.emptyMap();
        loadCache(postIds);
        return counterCacheOps.batchGetCounts(postIds, CacheFieldConstants.REPLY_COUNT);
    }

    // ======================== 点赞/收藏计数 ========================

    @Override
    public Map<Long, Map<String, Long>> getLikeAndCollectCount(List<Long> postIdList) {
        if (postIdList == null || postIdList.isEmpty()) return Collections.emptyMap();
        loadCache(postIdList);
        return counterCacheOps.batchGetLikeAndCollectCounts(postIdList);
    }

    @Override
    public Map<Long, Map<String, Boolean>> getIsCollectedOrLiked(List<Long> postIdList, Long userId) {
        if (postIdList == null || postIdList.isEmpty()) return Collections.emptyMap();
        loadCache(postIdList);
        Map<Long, Boolean> likeStatus = interactionCacheOps.batchIsMember(postIdList, userId, InteractionType.LIKE);
        Map<Long, Boolean> collectStatus = interactionCacheOps.batchIsMember(postIdList, userId, InteractionType.COLLECT);

        Map<Long, Map<String, Boolean>> result = new HashMap<>(postIdList.size());
        for (Long postId : postIdList) {
            Map<String, Boolean> status = new HashMap<>(2);
            status.put(CacheFieldConstants.INTERACTION_LIKE, likeStatus.getOrDefault(postId, false));
            status.put(CacheFieldConstants.INTERACTION_COLLECT, collectStatus.getOrDefault(postId, false));
            result.put(postId, status);
        }
        return result;
    }

    // ======================== 缓存预热 ========================

    @Override
    public void loadCache(Long postId) {
        if (postId == null) return;
        try {
            if (!contentCacheOps.exists(postId)) {
                PostsPO po = postsMapper.selectOne(new LambdaQueryWrapper<PostsPO>()
                        .eq(PostsPO::getPublicId, postId));
                if (po == null) return;
                List<Long> likedUserIds = postLikeRepository.findUserIdsByPostId(postId);
                List<Long> collectedUserIds = postCollectRepository.findUserIdsByPostId(postId);
                long likeCount = Math.max((long) likedUserIds.size(), po.getLikeCount() != null ? po.getLikeCount() : 0L);
                long collectCount = Math.max((long) collectedUserIds.size(), po.getCollectCount() != null ? po.getCollectCount() : 0L);

                List<Long> tagIds = postTagRelRepository.findByPostId(postId).stream()
                        .map(PostTag::getTagId).toList();
                contentCacheOps.loadSinglePostHash(postId, po, likeCount, collectCount, tagIds);
                interactionCacheOps.loadInteractionSet(postId, InteractionType.LIKE, likedUserIds);
                interactionCacheOps.loadInteractionSet(postId, InteractionType.COLLECT, collectedUserIds);
            } else {
                // 内容缓存已存在，仅检查互动 Set（TTL 可能不同步）
                interactionCacheOps.loadInteractionSet(postId, InteractionType.LIKE, postLikeRepository.findUserIdsByPostId(postId));
                interactionCacheOps.loadInteractionSet(postId, InteractionType.COLLECT, postCollectRepository.findUserIdsByPostId(postId));
            }
        } catch (Exception e) {
            log.warn("【帖子模块】加载单个帖子缓存失败，postId={}", postId, e);
        }
    }

    @Override
    public void loadCache(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) return;
        try {
            // 1. Pipeline 批量检查缺失情况
            List<Object> existsResults = redisTemplate.executePipelined(new SessionCallback<>() {
                @Override
                public Object execute(@NonNull RedisOperations operations) {
                    for (Long postId : postIds) {
                        operations.hasKey(PostConstants.Cache.DETAIL_PREFIX + postId);
                        operations.hasKey(InteractionType.LIKE.buildSetKey(postId));
                        operations.hasKey(InteractionType.COLLECT.buildSetKey(postId));
                    }
                    return null;
                }
            });

            List<Long> missingPostIds = new ArrayList<>();
            List<Long> missingLikePostIds = new ArrayList<>();
            List<Long> missingCollectPostIds = new ArrayList<>();

            for (int i = 0; i < postIds.size(); i++) {
                Long postId = postIds.get(i);
                Boolean postExists = (Boolean) existsResults.get(i * 3);
                Boolean likeExists = (Boolean) existsResults.get(i * 3 + 1);
                Boolean collectExists = (Boolean) existsResults.get(i * 3 + 2);
                if (postExists == null || !postExists) missingPostIds.add(postId);
                if (likeExists == null || !likeExists) missingLikePostIds.add(postId);
                if (collectExists == null || !collectExists) missingCollectPostIds.add(postId);
            }

            // 2. 批量预热点赞/收藏 Set
            interactionCacheOps.batchLoadMissingSets(missingLikePostIds, InteractionType.LIKE, postLikeRepository::findUserIdsByPostIds);
            interactionCacheOps.batchLoadMissingSets(missingCollectPostIds, InteractionType.COLLECT, postCollectRepository::findUserIdsByPostIds);

            // 3. 批量预热帖子主体 Hash（合并精准互动计数）
            if (!missingPostIds.isEmpty()) {
                List<PostsPO> pos = postsMapper.selectList(new LambdaQueryWrapper<PostsPO>()
                        .in(PostsPO::getPublicId, missingPostIds));
                if (pos != null && !pos.isEmpty()) {
                    List<PostTag> tagRels = postTagRelRepository.findByPostIds(missingPostIds);
                    Map<Long, List<Long>> tagIdsMap = tagRels == null ? Collections.emptyMap() :
                            tagRels.stream().collect(Collectors.groupingBy(
                                    PostTag::getPostId,
                                    Collectors.mapping(PostTag::getTagId, Collectors.toList())
                            ));
                    contentCacheOps.batchLoadMissingPostHash(missingPostIds, pos, tagIdsMap);
                }
            }

        } catch (Exception e) {
            log.warn("【帖子模块】批量预热缓存失败，postIds={}", postIds, e);
        }
    }

    // ======================== ZSet 分页查询 ========================

    @Override
    public void addToNewestZSet(Long postId) {
        queryZSetCacheOps.addToNewestZSet(postId);
    }

    @Override
    public void addToHotZSet(Long postId, double score) {
        queryZSetCacheOps.addToHotZSet(postId, score);
    }

    @Override
    public void addToHotZSet(Map<Long, Double> scoresMap) {
        queryZSetCacheOps.addToHotZSet(scoresMap);
    }


    @Override
    public Set<Long> getPostIdsFromNewest(int offset, int limit) {
        Set<Long> ids = queryZSetCacheOps.getPostIdsFromNewest(offset, limit);
        // 首页查询且 ZSet 为空 → 懒加载重建（Redis 重启/清空后自动恢复）
        if (ids.isEmpty() && offset == 0) {
            log.info("【帖子查询-临时日志】触发懒加载重建");
            rebuildNewestZSet();
            ids = queryZSetCacheOps.getPostIdsFromNewest(offset, limit);
        }
        return ids;
    }

    @Override
    public Set<Long> getPostIdsFromHot(int offset, int limit) {
        return queryZSetCacheOps.getPostIdsFromHot(offset, limit);
    }

    @Override
    public void rebuildHotZSet(Map<Long, Double> posts) {
        queryZSetCacheOps.rebuildHotZSet(posts);
    }

    @Override
    public void rebuildNewestZSet() {
        String lockKey = PostConstants.Cache.NEWEST_REBUILD_LOCK;
        try {
            // 分布式锁防并发重建，30 秒过期防死锁
           distributedLockUtil.executeWithLock(lockKey,1, 30, () -> {
               // 从 DB 拉取最近 5000 条正常状态帖子，按 ID 降序（ID 即时间序）
               List<PostsPO> pos = postsMapper.selectList(
                       new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PostsPO>()
                               .eq(PostsPO::getStatus, 1)
                               .orderByDesc(PostsPO::getId)
                               .last("limit 5000")
               );
               if (pos != null && !pos.isEmpty()) {
                   queryZSetCacheOps.rebuildNewestZSet(pos);
               }
           });

        } catch (Exception e) {
            log.warn("【帖子模块】重建最新帖子ZSet缓存异常", e);
        }
    }

    @Override
    public Long getRankFromHot(Long postId) {
        return queryZSetCacheOps.getRankFromHot(postId);
    }

    @Override
    public Long getRankFromNewest(Long postId) {
        return queryZSetCacheOps.getRankFromNewest(postId);
    }

    // ======================== 帖子内容 ========================

    @Override
    public PostVO getPostContent(Long postId) {
        return contentCacheOps.getPostContent(postId);
    }

    @Override
    public Map<Long, PostVO> batchGetPostContents(List<Long> postIds) {
        return contentCacheOps.batchGetPostContents(postIds);
    }

    @Override
    public Map<Long, String> batchGetTagIds(List<Long> postIds) {
        return contentCacheOps.batchGetTagIds(postIds);
    }

    // ======================== 定时同步调度缓存支持 ========================

    @Override
    public Set<String> scanChangedRunKeys() {
        String pattern = PostConstants.Cache.CHANGED_RUN_PREFIX + "*";
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

    @Override
    public boolean hasChangedKey() {
        return Boolean.TRUE.equals(redisTemplate.hasKey(PostConstants.Cache.CHANGED));
    }

    @Override
    public boolean renameChangedKey(String backupKey) {
        try {
            redisTemplate.rename(PostConstants.Cache.CHANGED, backupKey);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    @Override
    public Set<Long> getBackupPostIds(String backupKey) {
        Set<Object> members = redisTemplate.opsForSet().members(backupKey);
        if (members == null || members.isEmpty()) {
            return Collections.emptySet();
        }
        return members.stream()
                .filter(Objects::nonNull)
                .map(this::toLong)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @Override
    public void deleteBackupKey(String backupKey) {
        redisTemplate.delete(backupKey);
    }

    @Override
    public void removeBackupPostIds(String backupKey, List<Long> postIds) {
        if (postIds != null && !postIds.isEmpty()) {
            redisTemplate.opsForSet().remove(backupKey, postIds.toArray());
        }
    }

    @Override
    public void batchCachePostDetail(List<Post> posts) {
        if (posts == null || posts.isEmpty()) return;
        redisTemplate.executePipelined(new SessionCallback<Object>() {
            @Override
            public <K, V> Object execute(@NonNull RedisOperations<K, V> operations) throws DataAccessException {
                posts.stream()
                        .filter(Objects::nonNull)
                        .forEach(post -> {
                            String postKey = PostConstants.Cache.DETAIL_PREFIX + post.getId();
                            if (Boolean.TRUE.equals(redisTemplate.hasKey(postKey))) {
                                Map<String, Object> fields = new HashMap<>();
                                fields.put(CacheFieldConstants.LIKE_COUNT, post.getLikeCount());
                                fields.put(CacheFieldConstants.COLLECT_COUNT, post.getCollectCount());
                                fields.put(CacheFieldConstants.VIEW_COUNT, post.getViewCount());
                                fields.put(CacheFieldConstants.REPLY_COUNT, post.getReplyCount());
                                fields.put(CacheFieldConstants.HOT_SCORE, post.getHotScore());
                                redisTemplate.opsForHash().putAll(postKey, fields);
                            }
                        });
                return null;
            }
        });
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

    @Override
    public void savePostContent(PostVO vo) {
        contentCacheOps.savePostContent(vo);
    }

    @Override
    public void deletePostContent(Long postId) {
        contentCacheOps.deletePostContent(postId);
    }

    @Override
    public void cachePostStatus(Long postId, int status) {
        contentCacheOps.cachePostStatus(postId, status);
    }

    @Override
    public void removeFromQueryZSets(Long postId) {
        queryZSetCacheOps.removeFromZSets(postId);
    }

    @Override
    public List<Long> getActivePosts() {
        Set<Object> members = redisTemplate.opsForSet().members(PostConstants.Cache.ACTIVE_POST_KEYS);
        if(members.isEmpty())return List.of();
       return  members.stream().filter(Objects::nonNull).map(o->(Long)o).toList();
    }

    @Override
    public void putActive(Long postId) {
        redisTemplate.opsForSet().add(PostConstants.Cache.ACTIVE_POST_KEYS,postId);
    }
}

