package com.summit.stp.tag.application.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.summit.stp.post.infrastructure.constants.PostConstants;
import com.summit.stp.post.infrastructure.persistence.mapper.PostsMapper;
import com.summit.stp.tag.application.service.TagCacheProvider;
import com.summit.stp.tag.application.service.impl.cache.TagDetailCacheOps;
import com.summit.stp.tag.application.vo.TagVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 标签帖子列表缓存服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TagCacheProviderImpl implements TagCacheProvider {

    private final RedisTemplate<String, Object> redisTemplate;
    private final PostsMapper postsMapper;
    private final TagDetailCacheOps tagDetailCacheOps;


    /**
     * 全局热门帖子本地缓存 (防止高频请求击穿 Redis)
     */
    private final Cache<String, Map<Long, Double>> globalHotPostsCache = Caffeine.newBuilder()
            .expireAfterWrite(PostConstants.Business.TAG_GLOBAL_HOT_CACHE_TTL_SEC, TimeUnit.SECONDS)
            .maximumSize(PostConstants.Business.TAG_GLOBAL_HOT_CACHE_MAX_SIZE)
            .build();

    @Override
    public List<Long> getPostsHotTag(Long tagId, String cursor, Integer limit) {
        if (tagId == null) {
            return List.of();
        }
        int sizeLimit = limit != null && limit > 0 ? limit : PostConstants.Business.DEFAULT_PAGE_SIZE;
        String key = PostConstants.Cache.TAG_POSTS_PREFIX + tagId;

        // 1. 确保缓存存在，若不存在触发懒加载
        ensureCacheExists(tagId, key);

        // 2. 取出标签下全量帖子 ID
        Set<Object> rawTagPostIds = redisTemplate.opsForZSet().reverseRange(key, 0, -1);
        if (rawTagPostIds == null || rawTagPostIds.isEmpty()) {
            return List.of();
        }

        List<Long> tagPostIds = rawTagPostIds.stream()
                .map(o -> Long.parseLong(o.toString()))
                .toList();

        // 3. 获取全局热帖排序分数 Map
        String globalHotPosts = PostConstants.Cache.GLOBAL_HOT_POSTS_LOCAL_KEY;
        Map<Long, Double> globalHotMap = globalHotPostsCache.get(globalHotPosts, k -> loadGlobalHotPosts());
        if (globalHotMap == null || globalHotMap.isEmpty()) {
            // 降级：若全局热帖为空，直接按发布时间降序返回
            return tagPostIds.stream().limit(sizeLimit).toList();
        }

        // 4. 交集过滤并依热度降序排序 (热度相同以 ID 降序)
        List<Map.Entry<Long, Double>> matched = new ArrayList<>();
        for (Long pid : tagPostIds) {
            if (globalHotMap.containsKey(pid)) {
                matched.add(new AbstractMap.SimpleEntry<>(pid, globalHotMap.get(pid)));
            }
        }
        matched.sort((o1, o2) -> {
            int cmp = o2.getValue().compareTo(o1.getValue());
            return cmp != 0 ? cmp : o2.getKey().compareTo(o1.getKey());
        });

        // 5. 游标滚动分页截取
        int startIdx = parseCursorForHot(cursor, matched);
        if (startIdx >= matched.size()) {
            return List.of();
        }

        int endIdx = Math.min(startIdx + sizeLimit, matched.size());
        List<Long> result = new ArrayList<>(endIdx - startIdx);
        for (int i = startIdx; i < endIdx; i++) {
            result.add(matched.get(i).getKey());
        }
        return result;
    }

    @Override
    public List<Long> getPostsTag(Long tagId, String cursor, Integer limit) {
        if (tagId == null) {
            return List.of();
        }
        int sizeLimit = limit != null && limit > 0 ? limit : PostConstants.Business.DEFAULT_PAGE_SIZE;
        String key = PostConstants.Cache.TAG_POSTS_PREFIX + tagId;

        // 1. 第一页缺失触发懒加载
        if (cursor == null || cursor.isBlank()) {
            ensureCacheExists(tagId, key);
        }

        // 2. 游标转换与分页
        double maxScore = Double.MAX_VALUE;
        if (cursor != null && !cursor.isBlank()) {
            try {
                maxScore = Double.parseDouble(cursor) - PostConstants.Business.CURSOR_SCORE_OFFSET;
            } catch (NumberFormatException ignored) {
            }
        }

        Set<Object> ids = redisTemplate.opsForZSet().reverseRangeByScore(key, 0.0, maxScore, 0, sizeLimit);
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        return ids.stream()
                .map(o -> Long.parseLong(o.toString()))
                .toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addPostToTags(Long postId, List<Long> tagIds) {
        if (postId == null || tagIds == null || tagIds.isEmpty()) {
            return;
        }
        try {
            redisTemplate.executePipelined(new SessionCallback<Object>() {
                @Override
                public Object execute(@NonNull RedisOperations operations) {
                    for (Long tagId : tagIds) {
                        String key = PostConstants.Cache.TAG_POSTS_PREFIX + tagId;
                        operations.opsForZSet().add(key, postId, postId.doubleValue());
                        long expireSeconds = PostConstants.Business.TAG_POSTS_CACHE_TTL_SEC
                                + java.util.concurrent.ThreadLocalRandom.current().nextInt(PostConstants.Business.TAG_POSTS_CACHE_TTL_RANDOM_SEC);
                        operations.expire(key, expireSeconds, TimeUnit.SECONDS);
                    }
                    return null;
                }
            });
            log.info("【标签模块】发帖关联标签缓存更新, postId={}, tagIds={}", postId, tagIds);
        } catch (Exception e) {
            log.error("【标签模块】发帖关联标签缓存更新失败, postId={}, tagIds={}", postId,tagIds, e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void removePostFromTags(Long postId, List<Long> tagIds) {
        if (postId == null || tagIds == null || tagIds.isEmpty()) {
            return;
        }
        try {
            redisTemplate.executePipelined(new SessionCallback<Object>() {
                @Override
                public Object execute(@NonNull RedisOperations operations) {
                    for (Long tagId : tagIds) {
                        String key = PostConstants.Cache.TAG_POSTS_PREFIX + tagId;
                        operations.opsForZSet().remove(key, postId);
                    }
                    return null;
                }
            });
            log.info("【标签模块】删帖移除标签缓存更新, postId={}, tagIds={}", postId, tagIds);
        } catch (Exception e) {
            log.error("【标签模块】删帖移除标签缓存更新失败, postId={}", postId, e);
        }
    }

    private void ensureCacheExists(Long tagId, String key) {
        Boolean hasKey = redisTemplate.hasKey(key);
        if (Boolean.FALSE.equals(hasKey)) {
            rebuildTagZSet(tagId);
        }
    }

    @SuppressWarnings("unchecked")
    private void rebuildTagZSet(Long tagId) {
        String key = PostConstants.Cache.TAG_POSTS_PREFIX + tagId;
        try {
            List<Long> postIds = postsMapper.getPostsByTag(tagId, null, PostConstants.Business.TAG_POSTS_REBUILD_LIMIT);
            if (postIds != null && !postIds.isEmpty()) {
                redisTemplate.executePipelined(new SessionCallback<Object>() {
                    @Override
                    public Object execute(@NonNull RedisOperations operations) {
                        for (Long postId : postIds) {
                            operations.opsForZSet().add(key, postId, postId.doubleValue());
                        }
                        long expireSeconds = PostConstants.Business.TAG_POSTS_CACHE_TTL_SEC
                                + java.util.concurrent.ThreadLocalRandom.current().nextInt(PostConstants.Business.TAG_POSTS_CACHE_TTL_RANDOM_SEC);
                        operations.expire(key, expireSeconds, TimeUnit.SECONDS);
                        return null;
                    }
                });
                log.info("【标签模块】标签缓存懒加载重建成功, tagId={}, postsCount={}", tagId, postIds.size());
            }
        } catch (Exception e) {
            log.error("【标签模块】标签缓存懒加载重建失败, tagId={}", tagId, e);
        }
    }

    private Map<Long, Double> loadGlobalHotPosts() {
        try {
            Set<ZSetOperations.TypedTuple<Object>> range = redisTemplate.opsForZSet()
                    .reverseRangeWithScores(PostConstants.Cache.QUERY_HOT, 0, PostConstants.Business.TAG_GLOBAL_HOT_POSTS_LIMIT - 1);
            if (range == null || range.isEmpty()) {
                return Collections.emptyMap();
            }
            Map<Long, Double> result = new LinkedHashMap<>(range.size());
            for (ZSetOperations.TypedTuple<Object> t : range) {
                if (t.getValue() != null && t.getScore() != null) {
                    try {
                        result.put(Long.parseLong(t.getValue().toString()), t.getScore());
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            log.info("【标签模块】加载全局热帖成功, count={}", result.size());
            return result;
        } catch (Exception e) {
            log.error("【标签模块】加载全局热帖异常", e);
            return Collections.emptyMap();
        }
    }

    private int parseCursorForHot(String cursor, List<Map.Entry<Long, Double>> matched) {
        if (cursor == null || cursor.isBlank()) {
            return 0;
        }
        String[] parts = cursor.split("_");
        if (parts.length != 2) {
            return 0;
        }
        try {
            double cursorScore = Double.parseDouble(parts[0]);
            long cursorPostId = Long.parseLong(parts[1]);
            for (int i = 0; i < matched.size(); i++) {
                Map.Entry<Long, Double> entry = matched.get(i);
                double score = entry.getValue();
                long pid = entry.getKey();
                if (score < cursorScore || (score == cursorScore && pid < cursorPostId)) {
                    return i;
                }
            }
            return matched.size();
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public Map<Long, TagVO> batchGetTagDetails(List<Long> tagIds) {
        return tagDetailCacheOps.batchGetTagDetails(tagIds);
    }

    @Override
    public void batchSaveTagDetails(List<TagVO> tags) {
        tagDetailCacheOps.batchSaveTagDetails(tags);
    }

    @Override
    public void deleteTagDetail(Long tagId) {
        tagDetailCacheOps.deleteTagDetail(tagId);
    }
}

