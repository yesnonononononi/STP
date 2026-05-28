package com.summit.stp.post.application.service.impl;

import com.summit.stp.post.application.service.PostCacheProvider;
import com.summit.stp.post.domain.repository.PostLikeRepository;
import com.summit.stp.post.domain.repository.PostCollectRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.sql.Time;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
@SuppressWarnings("unchecked")
public class PostCacheProviderImpl implements PostCacheProvider {
    private final RedisTemplate<Object, Object> redisTemplate;
    private final ZSetOperations<Object, Object> redisSet;
    private final PostLikeRepository postLikeRepository;
    private final PostCollectRepository postCollectRepository;
    private final Long CACHE_TTL = 7L;

    public PostCacheProviderImpl(RedisTemplate<Object, Object> redisTemplate,
                                 PostLikeRepository postLikeRepository,
                                 PostCollectRepository postCollectRepository) {
        this.redisTemplate = redisTemplate;
        this.redisSet = redisTemplate.opsForZSet();
        this.postLikeRepository = postLikeRepository;
        this.postCollectRepository = postCollectRepository;
    }

    @Override
    public void like(Long postId, Long userId) {
        loadCache(postId);
        String key = buildLikeKey(postId);
        Boolean add = redisSet.add(key, userId, System.currentTimeMillis());
        if (Boolean.FALSE.equals(add)) {
            //取消点赞
            redisSet.remove(key, userId);
        }
        markCacheChanged(postId);
    }

    @Override
    public void collect(Long postId, Long userId) {
        loadCache(postId);
        String key = buildCollectKey(postId);
        Boolean add = redisSet.add(key, userId, System.currentTimeMillis());
        if (Boolean.FALSE.equals(add)) {
            //取消收藏
            redisSet.remove(key, userId);
        }
        markCacheChanged(postId);
    }

    @Override
    public boolean isCollected(Long postId, Long userId) {
        if (userId == null || userId.equals(-1L)) {
            return false;
        }
        String key = buildCollectKey(postId);
        return redisSet.score(key, userId) != null;
    }

    @Override
    public Long getLikeCount(Long postId) {
        String key = buildLikeKey(postId);
        Long size = redisSet.zCard(key);
        if (size == null) {
            return 0L;
        }
        boolean hasDummy = redisSet.score(key, -1L) != null;
        return hasDummy ? size - 1 : size;
    }

    @Override
    public Long getCollectCount(Long postId) {
        String key = buildCollectKey(postId);
        Long size = redisSet.zCard(key);
        if (size == null) {
            return 0L;
        }
        boolean hasDummy = redisSet.score(key, -1L) != null;
        return hasDummy ? size - 1 : size;
    }

    @Override
    public boolean isLiked(Long postId, Long userId) {
        if (userId == null || userId.equals(-1L)) {
            return false;
        }
        String key = buildLikeKey(postId);
        return redisSet.score(key, userId) != null;
    }

    @Override
    public void loadCache(Long postId) {
        String likeKey = buildLikeKey(postId);
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(likeKey))) {
            loadSet(likeKey, postLikeRepository.findUserIdsByPostId(postId));
        }

        String collectKey = buildCollectKey(postId);
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(collectKey))) {
            loadSet(collectKey, postCollectRepository.findUserIdsByPostId(postId));
        }
    }

    /**
     * 业务流程: <br>
     * 1. 配合 Redis 管道（Pipeline）技术的 hasKey 方法，批量检查传入的帖子 ID 列表中，有哪些帖子的点赞/收藏 ZSet 缓存不存在（即未初始化或已过期淘汰）；<br>
     * 2. 过滤并统计出缓存失效的帖子 ID 列表，【仅针对这部分失效的帖子】批量从数据库中一次性查询其对应的点赞用户和收藏用户关系（使用 SQL IN 子句，规避循环 N+1 SQL 查询）；<br>
     * 3. 再次利用 Pipeline 管道技术，批量将查询出的用户 ID 装配入 Redis 的 ZSet 缓存中（写入 -1L 占位符以防缓存穿透），并统一设置过期时间。
     *
     * @param postIds 帖子ID列表
     */
    @Override
    public void loadCache(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return;
        }

        // 1. 批量 Pipeline 检查哪些 key 在 Redis 中不存在
        List<Object> existsResults = redisTemplate.executePipelined(new SessionCallback<>() {
            @Override
            public Object execute(@NonNull RedisOperations operations) {
                for (Long postId : postIds) {
                    operations.hasKey(buildLikeKey(postId));
                    operations.hasKey(buildCollectKey(postId));
                }
                return null;
            }
        });

        // 2. 统计缺失缓存的 postId
        List<Long> missingLikePostIds = new ArrayList<>();
        List<Long> missingCollectPostIds = new ArrayList<>();

        for (int i = 0; i < postIds.size(); i++) {
            Long postId = postIds.get(i);
            Object likeExists = existsResults.get(i * 2);
            Object collectExists = existsResults.get(i * 2 + 1);

            if (likeExists == null || Boolean.FALSE.equals(likeExists)) {
                missingLikePostIds.add(postId);
            }
            if (collectExists == null || Boolean.FALSE.equals(collectExists)) {
                missingCollectPostIds.add(postId);
            }
        }

        // 3. 批量查询并写入 Redis
        // 3.1 加载缺失的点赞缓存
        if (!missingLikePostIds.isEmpty()) {
            Map<Long, List<Long>> likesMap = postLikeRepository.findUserIdsByPostIds(missingLikePostIds);
            redisTemplate.executePipelined(new SessionCallback<>() {
                @Override
                public Object execute(@NonNull RedisOperations operations) {
                    ZSetOperations zset = operations.opsForZSet();
                    for (Long postId : missingLikePostIds) {
                        String key = buildLikeKey(postId);
                        batchSet(zset, postId, key, likesMap);
                    }
                    return null;
                }
            });
        }

        // 3.2 加载缺失的收藏缓存
        if (!missingCollectPostIds.isEmpty()) {
            Map<Long, List<Long>> collectsMap = postCollectRepository.findUserIdsByPostIds(missingCollectPostIds);
            redisTemplate.executePipelined(new SessionCallback<>() {
                @Override
                public Object execute(@NonNull RedisOperations operations) {
                    ZSetOperations zset = operations.opsForZSet();
                    for (Long postId : missingCollectPostIds) {
                        String key = buildCollectKey(postId);
                        batchSet(zset, postId, key, collectsMap);
                    }
                    return null;
                }
            });
        }
    }

    @Override
    public Map<Long, Map<String, Long>> getLikeAndCollectCount(List<Long> postIdList) {
        Map<Long, Map<String, Long>> map = new HashMap<>(postIdList.size());
        List<Object> result = redisTemplate.executePipelined(new SessionCallback<>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                for (int i = 0; i < postIdList.size(); i++) {
                    Long postId = postIdList.get(i);
                    map.put(postId, new HashMap<>(2));
                    String collectKey = buildCollectKey(postId);
                    String likeKey = buildLikeKey(postId);
                    operations.opsForZSet().zCard(collectKey); // 获取收藏数
                    operations.opsForZSet().zCard(likeKey);
                }
                return null;
            }
        });
        for (int i = 0; i < map.size(); i++) {
            Long likeCount = (Long) result.get(i * 2 + 1 );
            Long collectCount = (Long) result.get(i * 2 );
            Long curPostId = postIdList.get(i);
            Map<String, Long> resMap = map.get(curPostId);
            resMap.put("LIKE", likeCount);
            resMap.put("COLLECT", collectCount);
        }
        return map;


    }

    @Override
    public Map<Long, Map<String,Boolean>> getIsCollectedOrLiked(List<Long> postIdList, Long userId) {
        Map<Long, Map<String,Boolean>> map = new HashMap<>(postIdList.size());
        List<Object> result = redisTemplate.executePipelined(new SessionCallback<>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                for (int i = 0; i < postIdList.size(); i++) {
                    Long postId = postIdList.get(i);
                    map.put(postId, new HashMap<>(2));
                    String collectKey = buildCollectKey(postId);
                    String likeKey = buildLikeKey(postId);
                    ZSetOperations set = operations.opsForZSet();
                    set.score(collectKey,userId); // 获取收藏数
                    set.score(likeKey,userId);
                }
                return null;
            }
        });
        for (int i = 0; i < map.size(); i++) {
            Boolean isCollect = result.get(i * 2) != null;
            Boolean isLike = result.get(i * 2 + 1) != null;
            Long curPostId = postIdList.get(i);
            Map<String, Boolean> stringBooleanMap = map.get(curPostId);
            stringBooleanMap.put(PostCacheProvider.COLLECT, isCollect);
            stringBooleanMap.put(PostCacheProvider.LIKE, isLike);
        }
        return map;

    }


    final String key = "post:changed";
    @Override
    public List<Object> getChangedList() {

        Set<Object> members = redisTemplate.opsForSet().members(key);
        return new ArrayList<>(members);
    }

    @Override
    public void markCacheChanged(Object postId) {
        redisTemplate.opsForSet().add(key, postId);
    }

    @Override
    public Set<Long> getLikeUserIds(Long postId) {
        String likeKey = buildLikeKey(postId);
        Set<Object> members = redisSet.range(likeKey, 0, -1);
        if (members == null) {
            return Collections.emptySet();
        }
        Set<Long> userIds = new HashSet<>();
        for (Object member : members) {
            if (member instanceof Long) {
                Long userId = (Long) member;
                if (!userId.equals(-1L)) {
                    userIds.add(userId);
                }
            } else if (member instanceof Integer) {
                Long userId = ((Integer) member).longValue();
                if (!userId.equals(-1L)) {
                    userIds.add(userId);
                }
            }
        }
        return userIds;
    }

    @Override
    public Set<Long> getCollectUserIds(Long postId) {
        String collectKey = buildCollectKey(postId);
        Set<Object> members = redisSet.range(collectKey, 0, -1);
        if (members == null) {
            return Collections.emptySet();
        }
        Set<Long> userIds = new HashSet<>();
        for (Object member : members) {
            if (member instanceof Long) {
                Long userId = (Long) member;
                if (!userId.equals(-1L)) {
                    userIds.add(userId);
                }
            } else if (member instanceof Integer) {
                Long userId = ((Integer) member).longValue();
                if (!userId.equals(-1L)) {
                    userIds.add(userId);
                }
            }
        }
        return userIds;
    }

    private void batchSet(ZSetOperations zset, Long postId, String key, Map<Long, List<Long>> likesMap) {  //likesMap => postId -> List<UserId>
        zset.add(key, -1L, 0.0);
        List<Long> userIds = likesMap.get(postId);
        if (userIds != null) {
            for (Long userId : userIds) {
                zset.add(key, userId, (double) System.currentTimeMillis());
            }
        }
        redisTemplate.expire(key, CACHE_TTL, TimeUnit.DAYS);
    }

    private void loadSet(String key, List<Long> userIdsByPostId) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            return;
        }
        redisSet.add(key, -1L, 0.0);
        if (userIdsByPostId != null && !userIdsByPostId.isEmpty()) {
            for (Long userId : userIdsByPostId) {
                redisSet.add(key, userId, System.currentTimeMillis());
            }
        }
        redisTemplate.expire(key, CACHE_TTL, TimeUnit.DAYS);  //7天的缓存过期时间,在这个期间,如果没有人点赞或者收藏本帖子,则缓存就会过期,下次访问时,会重新加载缓存
    }

    private String buildLikeKey(Long postId) {
        String REDIS_LIKE_POST_KEY = "post:like:";
        return REDIS_LIKE_POST_KEY + postId;
    }

    private String buildCollectKey(Long postId) {
        String REDIS_COLLECT_POST_KEY = "post:collect:";
        return REDIS_COLLECT_POST_KEY + postId;
    }
}
