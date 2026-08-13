package com.summit.stp.rank_board.application.service.impl;

import com.summit.stp.post.domain.model.Post;
import com.summit.stp.post.infrastructure.constants.PostConstants;
import com.summit.stp.rank_board.application.service.RankCacheProvider;
import com.summit.stp.rank_board.domain.model.RankBoard;
import com.summit.stp.tag.domain.model.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankCacheProviderImpl implements RankCacheProvider {
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public List<Long> getHotPostIds(int size) {
        String key = PostConstants.Cache.POST_HOT_ZSET;
        Set<Object> range = redisTemplate.opsForZSet().reverseRange(key, 0, size - 1);
        return range != null ? range.stream()
                .map(this::toLong)
                .filter(Objects::nonNull)
                .toList() : List.of();
    }

    @Override
    public void cacheHotPosts(List<Post> posts) {
        if (posts == null || posts.isEmpty()) {
            return;
        }
        String key = PostConstants.Cache.POST_HOT_ZSET;
        for (Post post : posts) {
            double score = post.getHotScore() != null ? post.getHotScore() : 0.0;
            redisTemplate.opsForZSet().add(key, post.getId(), score);
            redisTemplate.expire(key,7+ ThreadLocalRandom.current().nextInt(1), TimeUnit.DAYS);  //随机时间防止缓存雪崩
        }
    }

    @Override
    public List<Long> getHotTopicIds(int size) {
        String key = PostConstants.Cache.TOPIC_USE_ZSET;
        Set<Object> range = redisTemplate.opsForZSet().reverseRange(key, 0, size - 1);
        return range != null ? range.stream()
                .map(this::toLong)
                .filter(Objects::nonNull)
                .distinct()
                .toList() : List.of();
    }

    @Override
    public void cacheHotTopics(List<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return;
        }
        String key = PostConstants.Cache.TOPIC_USE_ZSET;
        for (Tag tag : tags) {
            double score = tag.getUseCount() != null ? tag.getUseCount().doubleValue() : 0.0;
            redisTemplate.opsForZSet().add(key, tag.getId(), score);
            redisTemplate.expire(key, 7 + ThreadLocalRandom.current().nextInt(1), TimeUnit.DAYS);
        }
    }

    @Override
    public void incrementTopicScore(Long tagId, double delta) {
        if (tagId == null) {
            return;
        }
        String key = PostConstants.Cache.TOPIC_USE_ZSET;
        redisTemplate.opsForZSet().incrementScore(key, tagId, delta);
    }

    @Override
    public Double getTopicScore(Long tagId) {
        if (tagId == null) {
            return null;
        }
        String key = PostConstants.Cache.TOPIC_USE_ZSET;
        return redisTemplate.opsForZSet().score(key, tagId);
    }




    @Override
    public void cachePostScore(Long postId, double score) {
        if (postId == null) {
            return;
        }
        String key = PostConstants.Cache.POST_HOT_ZSET;
        redisTemplate.opsForZSet().add(key, postId, score);
        //过期时间
        redisTemplate.expire(key,7+ThreadLocalRandom.current().nextInt(1), TimeUnit.DAYS);
    }

    @Override
    public void cachePostsScore(Map<Long, Double> posts) {
        if (posts == null || posts.isEmpty()) {
            return;
        }
        String key = PostConstants.Cache.POST_HOT_ZSET;
        Set<ZSetOperations.TypedTuple<Object>> tuples = posts.entrySet().stream()
                .map(e -> ZSetOperations.TypedTuple.of((Object) e.getKey(), e.getValue()))
                .collect(Collectors.toSet());
        redisTemplate.opsForZSet().add(key, tuples);
        redisTemplate.expire(key, 7 + ThreadLocalRandom.current().nextInt(1), TimeUnit.DAYS);
        log.info("【排行榜模块】动作：批量更新帖子热度得分, count={}", posts.size());
    }

    @Override
    public Double getPostScore(Long postId) {
        if (postId == null) {
            return null;
        }
        String key = PostConstants.Cache.POST_HOT_ZSET;
        return redisTemplate.opsForZSet().score(key, postId);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void updateCreatorRank(List<RankBoard> res) {
        redisTemplate.executePipelined(new SessionCallback<>() {
            @Override
            public Object execute(@NonNull RedisOperations operations) throws DataAccessException {
                for (RankBoard rb : res) {
                    Long uid = rb.getEntityId();
                    String key = buildCreatorRankKey(uid);
                    operations.opsForZSet().add(key, uid, rb.getScore());
                }
                return null;
            }
        });
    }


    private Long toLong(Object obj) {
        if (obj instanceof Long val) return val;
        if (obj instanceof Integer val) return val.longValue();
        if (obj instanceof Double val) return val.longValue();
        if (obj instanceof String val) {
            try {
                return Long.parseLong(val);
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }
    private String buildCreatorRankKey(Long uid){
        return PostConstants.Cache.RANK_CREATOR_KEY + uid;
    }
}
