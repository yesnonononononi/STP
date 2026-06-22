package com.summit.stp.post.application.service.impl;

import com.summit.stp.post.application.service.PostCacheProvider;
import com.summit.stp.post.domain.repository.PostCollectRepository;
import com.summit.stp.post.domain.repository.PostLikeRepository;
import com.summit.stp.post.infrastructure.persistence.mapper.PostsMapper;
import com.summit.stp.post.infrastructure.persistence.po.PostsPO;
import com.summit.stp.shared.constants.RedisConstants;
import com.summit.stp.shared.ThreadContext.UserHolder;
import com.summit.stp.shared.util.IpUtil;
import org.jspecify.annotations.NonNull;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@SuppressWarnings("unchecked")
public class PostCacheProviderImpl implements PostCacheProvider {
    private final RedisTemplate<Object, Object> redisTemplate;
    private final ZSetOperations<Object, Object> redisSet;
    private final PostLikeRepository postLikeRepository;
    private final PostCollectRepository postCollectRepository;
    private final PostsMapper postsMapper;
    private final Long CACHE_TTL = 7L;

    public PostCacheProviderImpl(RedisTemplate<Object, Object> redisTemplate,
                                 PostLikeRepository postLikeRepository,
                                 PostCollectRepository postCollectRepository,
                                 PostsMapper postsMapper) {
        this.redisTemplate = redisTemplate;
        this.redisSet = redisTemplate.opsForZSet();
        this.postLikeRepository = postLikeRepository;
        this.postCollectRepository = postCollectRepository;
        this.postsMapper = postsMapper;
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

        loadViewCache(postId);
    }

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
    public Map<Long, Map<String, Boolean>> getIsCollectedOrLiked(List<Long> postIdList, Long userId) {
        Map<Long, Map<String, Boolean>> map = new HashMap<>(postIdList.size());
        List<Object> result = redisTemplate.executePipelined(new SessionCallback<>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                for (int i = 0; i < postIdList.size(); i++) {
                    Long postId = postIdList.get(i);
                    map.put(postId, new HashMap<>(2));
                    String collectKey = buildCollectKey(postId);
                    String likeKey = buildLikeKey(postId);
                    ZSetOperations set = operations.opsForZSet();
                    set.score(collectKey, userId); // 获取收藏数
                    set.score(likeKey, userId);
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
    public void markLikeCollectChanged(Object postId) {
        redisTemplate.opsForSet().add(RedisConstants.Post.LIKE_COLLECT_CHANGED, postId);
    }

    @Override
    public void markViewChanged(Object postId) {
        redisTemplate.opsForSet().add(RedisConstants.Post.VIEW_CHANGED, postId);
    }

    @Override
    public void markReplyChanged(Object postId) {
        redisTemplate.opsForSet().add(RedisConstants.Post.REPLY_CHANGED, postId);
    }

    @Override
    public Set<Long> getLikeUserIds(Long postId) {
        String likeKey = buildLikeKey(postId);
        return getLongs(likeKey);
    }

    @Override
    public Set<Long> getCollectUserIds(Long postId) {
        String collectKey = buildCollectKey(postId);
        return getLongs(collectKey);
    }

    @NonNull
    private Set<Long> getLongs(String collectKey) {
        Set<Object> members = redisSet.range(collectKey, 0, -1);
        if (members == null) {
            return Collections.emptySet();
        }
        Set<Long> userIds = new HashSet<>();
        for (Object member : members) {
            if (member instanceof Long userId) {
                if (!userId.equals(-1L)) {
                    userIds.add(userId);
                }
            } else if (member instanceof Integer userIdInt) {
                Long userId = userIdInt.longValue();
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
        redisTemplate.expire(key, CACHE_TTL, TimeUnit.DAYS);  //7天的缓存过期时间
    }

    private String buildLikeKey(Long postId) {
        String REDIS_LIKE_POST_KEY = "post:like:";
        return REDIS_LIKE_POST_KEY + postId;
    }

    private String buildCollectKey(Long postId) {
        String REDIS_COLLECT_POST_KEY = "post:collect:";
        return REDIS_COLLECT_POST_KEY + postId;
    }

    // Tip: 浏览数防刷及加载逻辑实现

    private String buildViewKey(Long postId) {
        return RedisConstants.Post.VIEW + postId;
    }

    private void loadViewCache(Long postId) {
        String viewKey = buildViewKey(postId);
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(viewKey))) {
            PostsPO po = postsMapper.selectById(postId);
            long dbViewCount = (po != null && po.getViewCount() != null) ? po.getViewCount() : 0L;
            redisTemplate.opsForValue().set(viewKey, dbViewCount, CACHE_TTL, TimeUnit.DAYS);
        }
    }

    @Override
    public void incrViewCount(Long postId) {
        if (postId == null) return;
        
        Long userId = (UserHolder.getUser() != null) ? UserHolder.getUser().getId() : null;
        String limitKey;
        if (userId != null) {
            // Tip: 登录用户防刷 limit key，结构为 post:view:limit:{userid}，有效防止同一用户刷屏
            limitKey = "post:view:limit:" + userId;
        } else {
            // Tip: 游客防刷 limit key，结构为 post:view:limit:{ip}，根据请求IP防刷
            String ip = "unknown";
            try {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    ip = IpUtil.getIpAddr(attributes.getRequest());
                }
            } catch (Exception ignored) {}
            limitKey = "post:view:limit:" + ip;
        }

        // Tip: 判断当前帖子 ID 是否已存在于该用户/IP 今日浏览记录 Set 中
        Boolean hasVisited = redisTemplate.opsForSet().isMember(limitKey, postId);
        if (Boolean.TRUE.equals(hasVisited)) {
            // Tip: 重复访问则直接拦截自增
            return;
        }

        // Tip: 将帖子 ID 计入该 Set 集合
        redisTemplate.opsForSet().add(limitKey, postId);
        
        // Tip: 若今日第一次产生浏览记录，为 Set 设置 1 天的过期时间
        Long size = redisTemplate.opsForSet().size(limitKey);
        if (size != null && size == 1) {
            redisTemplate.expire(limitKey, 1, TimeUnit.DAYS);
        }

        // Tip: 帖子总浏览数的 String 缓存自增 1
        String key = buildViewKey(postId);
        Boolean hasKey = redisTemplate.hasKey(key);
        if (Boolean.FALSE.equals(hasKey)) {
            loadViewCache(postId);
        }
        redisTemplate.opsForValue().increment(key);
        
        // Tip: 标记帖子状态发生改变，待定时同步任务同步到数据库
        markViewChanged(postId);
    }

    @Override
    public Long getViewCount(Long postId) {
        if (postId == null) return 0L;
        String key = buildViewKey(postId);
        Object val = redisTemplate.opsForValue().get(key);
        if (val == null) {
            loadViewCache(postId);
            val = redisTemplate.opsForValue().get(key);
        }
        if (val instanceof Number num) {
            return num.longValue();
        } else if (val instanceof String str) {
            try {
                return Long.parseLong(str);
            } catch (NumberFormatException ignored) {}
        }
        return 0L;
    }

    @Override
    public Map<Long, Long> getViewCounts(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptyMap();
        }
        
        // Tip: 批量 Pipeline 从 Redis 的 String 缓存中获取各帖子的浏览计数值
        List<Object> results = redisTemplate.executePipelined(new SessionCallback<>() {
            @Override
            public Object execute(@NonNull RedisOperations operations) {
                for (Long postId : postIds) {
                    operations.opsForValue().get(buildViewKey(postId));
                }
                return null;
            }
        });

        Map<Long, Long> viewMap = new HashMap<>(postIds.size());
        List<Long> missingIds = new ArrayList<>();
        
        for (int i = 0; i < postIds.size(); i++) {
            Long postId = postIds.get(i);
            Object val = results.get(i);
            if (val == null) {
                missingIds.add(postId);
            } else {
                if (val instanceof Number num) {
                    viewMap.put(postId, num.longValue());
                } else if (val instanceof String str) {
                    try {
                        viewMap.put(postId, Long.parseLong(str));
                    } catch (NumberFormatException e) {
                        viewMap.put(postId, 0L);
                    }
                } else {
                    viewMap.put(postId, 0L);
                }
            }
        }

        // Tip: 如果存在缺失的缓存，批量查库回源并使用 Pipeline 重新热装配入 Redis
        if (!missingIds.isEmpty()) {
            List<PostsPO> pos = postsMapper.selectBatchIds(missingIds);
            Map<Long, Long> dbViews = pos.stream().collect(Collectors.toMap(PostsPO::getId, po -> po.getViewCount() != null ? po.getViewCount().longValue() : 0L));
            
            redisTemplate.executePipelined(new SessionCallback<>() {
                @Override
                public Object execute(@NonNull RedisOperations operations) {
                    for (Long postId : missingIds) {
                        long dbView = dbViews.getOrDefault(postId, 0L);
                        operations.opsForValue().set(buildViewKey(postId), dbView, CACHE_TTL, TimeUnit.DAYS);
                    }
                    return null;
                }
            });
            for (Long postId : missingIds) {
                viewMap.put(postId, dbViews.getOrDefault(postId, 0L));
            }
        }
        return viewMap;
    }
}
