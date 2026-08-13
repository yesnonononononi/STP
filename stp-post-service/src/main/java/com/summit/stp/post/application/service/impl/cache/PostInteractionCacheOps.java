package com.summit.stp.post.application.service.impl.cache;

import com.summit.stp.post.infrastructure.constants.PostConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 帖子互动缓存操作（点赞/收藏统一模板），消除原有的对称重复代码
 */
@Slf4j
@Component
@SuppressWarnings("unchecked")
public class PostInteractionCacheOps {

    private final RedisTemplate<String, Object> redisTemplate;

    public PostInteractionCacheOps(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 切换互动状态（点赞/收藏 toggle）
     * 0 : 未互动
     * 1 : 已互动
     *
     * @return 互动状态，标识是否是第一次互动
     */
    public boolean toggle(Long postId, Long userId, InteractionType type) {
        String setKey = type.buildSetKey(postId);
        String postKey = PostConstants.Cache.DETAIL_PREFIX + postId;
        try {
            Double score = redisTemplate.opsForZSet().score(setKey, userId);
            //如果score不存在,意味着是第一次互动且未互动成功
            //否则,score == 1 : 已经互动,需要取消 score == 0 : 未互动,需要添加
            if (score == null || score == 0.0) {
                redisTemplate.opsForZSet().add(setKey, userId, 1.0);
                redisTemplate.opsForHash().increment(postKey, type.getCountField(), 1);
            } else if (score == 1.0) {
                redisTemplate.opsForZSet().remove(setKey, userId);
                Long newCount = redisTemplate.opsForHash().increment(postKey, type.getCountField(), -1);
                if (newCount != null && newCount < 0) {
                    redisTemplate.opsForHash().put(postKey, type.getCountField(), 0L);
                }
            }
            return score == null;


        } catch (Exception e) {
            log.warn("【帖子模块】互动写入异常，type={}，动作：自增自减", type, e);
            throw e;
        }
    }

    /**
     * 判断用户是否已互动
     */
    public boolean isMember(Long postId, Long userId, InteractionType type) {
        if (userId == null || userId.equals(-1L)) {
            return false;
        }
        try {
            Double score = redisTemplate.opsForZSet().score(type.buildSetKey(postId), userId);
            return score != null && score == 1.0;
        } catch (Exception e) {
            log.warn("【帖子模块】判断互动状态异常，type={}，动作：isMember", type, e);
            return false;
        }
    }

    /**
     * 批量获取互动用户 ID 集合
     */
    public Map<Long, Set<Long>> batchGetUserIds(List<Long> postIdList, InteractionType type) {
        List<Object> list = redisTemplate.executePipelined(new SessionCallback<>() {
            @Override
            public Object execute(@NonNull RedisOperations operations) throws DataAccessException {
                for (Long postId : postIdList) {
                    operations.opsForZSet().rangeByScore(type.buildSetKey(postId), 1.0, 1.0);
                }
                return null;
            }
        });

        Map<Long, Set<Long>> result = new HashMap<>(postIdList.size());
        for (int i = 0; i < postIdList.size(); i++) {
            Long postId = postIdList.get(i);
            Object raw = list.get(i);
            if (raw instanceof Collection<?> s && !s.isEmpty()) {
                Set<Long> filtered = new HashSet<>();
                for (Object member : s) {
                    if(member == null)continue;
                    Long uid = Long.valueOf(member.toString());
                    if (!uid.equals(-1L)) {
                        filtered.add(uid);
                    }
                }
                result.put(postId, filtered);
            }
        }
        return result;
    }

    /**
     * 批量判断用户是否已互动
     */
    public Map<Long, Boolean> batchIsMember(List<Long> postIdList, Long userId, InteractionType type) {
        if (userId == null || userId.equals(-1L)) {
            Map<Long, Boolean> map = new HashMap<>(postIdList.size());
            for (Long postId : postIdList) {
                map.put(postId, false);
            }
            return map;
        }
        List<Object> results = redisTemplate.executePipelined(new SessionCallback<>() {
            @Override
            public Object execute(@NonNull RedisOperations operations) {
                for (Long postId : postIdList) {
                    operations.opsForZSet().score(type.buildSetKey(postId), userId);
                }
                return null;
            }
        });

        Map<Long, Boolean> map = new HashMap<>(postIdList.size());
        for (int i = 0; i < postIdList.size(); i++) {
            Object raw = results.get(i);
            boolean isMember = false;
            if (raw instanceof Double score) {
                isMember = (score == 1.0);
            }
            map.put(postIdList.get(i), isMember);
        }
        return map;
    }

    /**
     * 加载单个帖子的互动 ZSet 缓存（从 DB 预热）
     */
    public void loadInteractionSet(Long postId, InteractionType type, List<Long> userIdsFromDb) {
        String setKey = type.buildSetKey(postId);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(setKey))) {
            return;
        }
        redisTemplate.opsForZSet().add(setKey, -1L, 1.0);
        if (userIdsFromDb != null && !userIdsFromDb.isEmpty()) {
            for (Long uid : userIdsFromDb) {
                redisTemplate.opsForZSet().add(setKey, uid, 1.0);
            }
        }
        redisTemplate.expire(setKey, 7, TimeUnit.DAYS);
    }

    /**
     * 批量加载缺失的互动 ZSet 缓存（Pipeline 批量预热）
     */
    public void batchLoadMissingSets(
            List<Long> missingPostIds,
            InteractionType type,
            Function<List<Long>, Map<Long, List<Long>>> dbBatchLoader
    ) {
        if (missingPostIds.isEmpty()) {
            return;
        }
        Map<Long, List<Long>> userMap = dbBatchLoader.apply(missingPostIds);
        redisTemplate.executePipelined(new SessionCallback<>() {
            @Override
            public Object execute(@NonNull RedisOperations operations) {
                for (Long pid : missingPostIds) {
                    String setKey = type.buildSetKey(pid);
                    operations.opsForZSet().add(setKey, -1L, 1.0);
                    List<Long> users = userMap.getOrDefault(pid, Collections.emptyList());
                    for (Long uid : users) {
                        operations.opsForZSet().add(setKey, uid, 1.0);
                    }
                    operations.expire(setKey, 7, TimeUnit.DAYS);
                }
                return null;
            }
        });
    }

    /**
     * 标记互动已变更（加入 changed set）
     */
    public void markChanged(Object postId, String changedSetKey) {
        redisTemplate.opsForSet().add(changedSetKey, postId);
    }
}
