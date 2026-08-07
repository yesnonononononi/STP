package com.summit.stp.post.application.service.impl.cache;

import com.summit.stp.post.infrastructure.constants.PostConstants;
import com.summit.stp.post.infrastructure.persistence.po.PostsPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 帖子分页查询 ZSet 缓存操作（最新/热门列表）
 */
@Slf4j
@Component
public class PostQueryZSetCacheOps {

    /** ZSet 最大容量，超出淘汰 */
    private static final long MAX_CAPACITY = 5000L;

    private final ZSetOperations<String, Object> zSetOps;
    private final RedisTemplate<String, Object> redisTemplate;

    public PostQueryZSetCacheOps(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.zSetOps = redisTemplate.opsForZSet();
    }

    /**
     * 添加帖子到最新 ZSet（score 用毫秒时间戳保证无精度丢失地按时间降序排列）
     */
    public void addToNewestZSet(Long postId) {
        addToZSet(PostConstants.Cache.QUERY_NEWEST, postId, (double) System.currentTimeMillis());
    }

    /**
     * 添加帖子到热门 ZSet
     */
    public void addToHotZSet(Long postId, double score) {
        addToZSet(PostConstants.Cache.QUERY_HOT, postId, score);
    }

    /**
     * 从最新 ZSet 按偏移量分页获取帖子 ID（降序）
     */
    public Set<Long> getPostIdsFromNewest(int offset, int limit) {
        try {
            Set<Object> range = zSetOps.reverseRange(PostConstants.Cache.QUERY_NEWEST, offset, (long) offset + limit - 1);
            return toLinkedLongSet(range);
        } catch (Exception e) {
            log.warn("【帖子模块】Redis服务异常，动作：查询最新帖子缓存", e);
            return Collections.emptySet();
        }
    }

    /**
     * 从热门 ZSet 按偏移量分页获取帖子 ID（降序）
     */
    public Set<Long> getPostIdsFromHot(int offset, int limit) {
        try {
            Set<Object> range = zSetOps.reverseRange(PostConstants.Cache.QUERY_HOT, offset, (long) offset + limit - 1);
            return toLinkedLongSet(range);
        } catch (Exception e) {
            log.warn("【帖子模块】Redis服务异常，动作：查询热门帖子缓存", e);
            return Collections.emptySet();
        }
    }

    /**
     * 重建热门 ZSet 缓存
     */
    public void rebuildHotZSet(Map<Long, Double> posts) {
        try {
            String key = PostConstants.Cache.QUERY_HOT;
            redisTemplate.delete(key);
            if (posts != null && !posts.isEmpty()) {
                posts.forEach((id, score) -> zSetOps.add(key, id, score));
                trimZSet(key);
            }
        } catch (Exception e) {
            log.warn("【帖子模块】Redis服务异常，动作：重建热门帖子缓存", e);
        }
    }

    /**
     * 重建最新 ZSet 缓存（懒加载触发：ZSet 为空时从 DB 批量加载最近的正常公开帖子重建）
     */
    public void rebuildNewestZSet(List<PostsPO> pos) {
        try {
            String key = PostConstants.Cache.QUERY_NEWEST;
            redisTemplate.delete(key);
            if (pos != null && !pos.isEmpty()) {
                pos.forEach(po -> zSetOps.add(key, po.getPublicId(), (double) po.getCreateTime().getTime()));
                trimZSet(key);
            }
        } catch (Exception e) {
            log.warn("【帖子模块】Redis服务异常，动作：重建最新帖子缓存", e);
        }
    }



    /**
     * 获取帖子在热门列表中的排名（降序，0-based）
     */
    public Long getRankFromHot(Long postId) {
        try {
            return zSetOps.reverseRank(PostConstants.Cache.QUERY_HOT, postId);
        } catch (Exception e) {
            log.warn("【帖子模块】Redis服务异常，动作：获取热门帖子排行", e);
            return null;
        }
    }

    /**
     * 获取帖子在最新列表中的排名（降序，0-based）
     */
    public Long getRankFromNewest(Long postId) {
        try {
            return zSetOps.reverseRank(PostConstants.Cache.QUERY_NEWEST, postId);
        } catch (Exception e) {
            log.warn("【帖子模块】Redis服务异常，动作：获取最新帖子排行", e);
            return null;
        }
    }



    /**
     * 从最新和热门 ZSet 缓存中移除指定帖子 ID
     */
    public void removeFromZSets(Long postId) {
        try {
            zSetOps.remove(PostConstants.Cache.QUERY_NEWEST, postId);
            zSetOps.remove(PostConstants.Cache.QUERY_HOT, postId);
        } catch (Exception e) {
            log.warn("【帖子模块】Redis服务异常，动作：从最新/热门缓存中移除帖子，postId={}", postId, e);
        }
    }

    private void addToZSet(String key, Long postId, double score) {
        try {
            zSetOps.add(key, postId, score);
            trimZSet(key);
        } catch (Exception e) {
            log.warn("【帖子模块】Redis服务异常，动作：添加帖子到ZSet缓存，key={}", key, e);
        }
    }

    private void trimZSet(String key) {
        zSetOps.removeRange(key, 0, -MAX_CAPACITY - 1);
    }

    private Set<Long> toLinkedLongSet(Set<Object> range) {
        if (range == null || range.isEmpty()) {
            return Collections.emptySet();
        }
        return range.stream()
                .map(item -> Long.valueOf(item.toString()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
