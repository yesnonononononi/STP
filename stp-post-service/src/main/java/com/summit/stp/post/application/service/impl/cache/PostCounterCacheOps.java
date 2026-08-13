package com.summit.stp.post.application.service.impl.cache;

import com.summit.stp.common.ThreadContext.UserHolder;
import com.summit.stp.common.constants.CacheFieldConstants;
import com.summit.stp.common.util.IpUtil;
import com.summit.stp.post.infrastructure.constants.PostConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 帖子计数缓存操作（浏览数/回复数），基于帖子 Hash 结构读写
 */
@Slf4j
@Component
@SuppressWarnings("unchecked")
public class PostCounterCacheOps {

    private final RedisTemplate<String, Object> redisTemplate;

    public PostCounterCacheOps(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 自增浏览数（带用户/IP 去重限制，24 小时冷却）
     */
    public void incrViewCount(Long postId) {
        String identity = resolveViewIdentity();
        String limitKey = PostConstants.Cache.VIEW_LIMIT_PREFIX + postId + ":" + identity;
        String postKey = PostConstants.Cache.DETAIL_PREFIX + postId;
        try {
            Boolean added = redisTemplate.opsForValue().setIfAbsent(limitKey, "1", 1, TimeUnit.DAYS);
            if (Boolean.TRUE.equals(added)) {
                redisTemplate.opsForHash().increment(postKey, CacheFieldConstants.VIEW_COUNT, 1L);
            }
        } catch (Exception e) {
            log.warn("【帖子模块】自增浏览数异常，postId={}", postId, e);
        }
    }

    /**
     * 自增回复数
     */
    public void incrReplyCount(Long postId) {
        incrementField(postId, CacheFieldConstants.REPLY_COUNT, 1L);
    }

    /**
     * 自减回复数
     */
    public void decrReplyCount(Long postId) {
        incrementField(postId, CacheFieldConstants.REPLY_COUNT, -1L);
    }

    /**
     * 批量获取指定计数字段的值（使用 Pipeline 批量查询）
     */
    public Map<Long, Long> batchGetCounts(List<Long> postIds, String field) {
        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, Long> result = new HashMap<>(postIds.size());
        try {
            List<Object> pipelineResults = redisTemplate.executePipelined(new SessionCallback<>() {
                @Override
                public Object execute(@org.springframework.lang.NonNull RedisOperations operations) {
                    for (Long postId : postIds) {
                        operations.opsForHash().get(PostConstants.Cache.DETAIL_PREFIX + postId, field);
                    }
                    return null;
                }
            });
            for (int i = 0; i < postIds.size(); i++) {
                Object val = pipelineResults.get(i);
                result.put(postIds.get(i), val != null ? Long.parseLong(val.toString()) : 0L);
            }
        } catch (Exception e) {
            log.warn("【帖子模块】批量读取计数缓存异常，field={}", field, e);
        }
        return result;
    }

    /**
     * 批量获取点赞数和收藏数（使用 Pipeline 批量查询）
     */
    public Map<Long, Map<String, Long>> batchGetLikeAndCollectCounts(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, Map<String, Long>> map = new HashMap<>(postIds.size());
        try {
            List<Object> pipelineResults = redisTemplate.executePipelined(new SessionCallback<>() {
                @Override
                public Object execute(@org.springframework.lang.NonNull RedisOperations operations) {
                    for (Long postId : postIds) {
                        operations.opsForHash().multiGet(
                                PostConstants.Cache.DETAIL_PREFIX + postId,
                                List.of(CacheFieldConstants.LIKE_COUNT, CacheFieldConstants.COLLECT_COUNT)
                        );
                    }
                    return null;
                }
            });
            for (int i = 0; i < postIds.size(); i++) {
                Long postId = postIds.get(i);
                long likeCount = 0L;
                long collectCount = 0L;
                if (pipelineResults != null && i < pipelineResults.size()) {
                    Object rawList = pipelineResults.get(i);
                    if (rawList instanceof List<?> list && !list.isEmpty()) {
                        likeCount = parseLongSafely((List<Object>) list, 0);
                        collectCount = parseLongSafely((List<Object>) list, 1);
                    }
                }
                Map<String, Long> countMap = new HashMap<>(2);
                countMap.put(CacheFieldConstants.LIKE_COUNT, likeCount);
                countMap.put(CacheFieldConstants.COLLECT_COUNT, collectCount);
                map.put(postId, countMap);
            }
        } catch (Exception e) {
            log.warn("【帖子模块】批量读取点赞收藏计数缓存异常，动作：执行单条兜底读取", e);
            for (Long postId : postIds) {
                try {
                    List<Object> res = redisTemplate.opsForHash().multiGet(
                            PostConstants.Cache.DETAIL_PREFIX + postId,
                            List.of(CacheFieldConstants.LIKE_COUNT, CacheFieldConstants.COLLECT_COUNT)
                    );
                    long likeCount = parseLongSafely(res, 0);
                    long collectCount = parseLongSafely(res, 1);
                    Map<String, Long> countMap = new HashMap<>(2);
                    countMap.put(CacheFieldConstants.LIKE_COUNT, likeCount);
                    countMap.put(CacheFieldConstants.COLLECT_COUNT, collectCount);
                    map.put(postId, countMap);
                } catch (Exception ex) {
                    Map<String, Long> countMap = new HashMap<>(2);
                    countMap.put(CacheFieldConstants.LIKE_COUNT, 0L);
                    countMap.put(CacheFieldConstants.COLLECT_COUNT, 0L);
                    map.put(postId, countMap);
                }
            }
        }
        return map;
    }


    private void incrementField(Long postId, String field, long delta) {
        String key = PostConstants.Cache.DETAIL_PREFIX + postId;
        try {
            redisTemplate.opsForHash().increment(key, field, delta);
        } catch (Exception e) {
            log.warn("【帖子模块】计数自增异常，postId={}，field={}，delta={}", postId, field, delta, e);
        }
    }

    private String resolveViewIdentity() {
        Long userId = UserHolder.getUser().getId();
        if (userId != null) {
            return "u:" + userId;
        }
        String ip = "unknown";
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                ip = IpUtil.getIpAddr(attrs.getRequest());
            }
        } catch (Exception ignored) {}
        return "ip:" + ip;
    }

    private long parseLongSafely(List<Object> list, int index) {
        if (list != null && list.size() > index && list.get(index) != null) {
            return Long.parseLong(list.get(index).toString());
        }
        return 0L;
    }
}
