package com.summit.stp.post.application.service.impl.cache;

import com.summit.stp.common.application.domain.model.UserSession;
import com.summit.stp.common.auth.UserHolder;
import com.summit.stp.common.constants.CacheFieldConstants;
import com.summit.stp.post.infrastructure.constants.PostConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link PostCounterCacheOps} 的纯单元测试（Mockito，不依赖 Redis）。
 *
 * <p>覆盖浏览数去重自增、回复数自增/自减、批量读取计数等分支，
 * 为 PIT 变异测试提供有杀伤力的断言。</p>
 */
@SuppressWarnings({"unchecked", "rawtypes"})
class PostCounterCacheOpsTest {

    private static final long POST_ID = 5L;

    private static final long USER_ID = 7L;

    private RedisTemplate<String, Object> redisTemplate;

    private ValueOperations<String, Object> valueOps;

    private HashOperations<String, Object, Object> hashOps;

    private PostCounterCacheOps ops;

    private String postKey;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(RedisTemplate.class);
        valueOps = mock(ValueOperations.class);
        hashOps = mock(HashOperations.class);
        ops = new PostCounterCacheOps(redisTemplate);
        postKey = PostConstants.Cache.DETAIL_PREFIX + POST_ID;
        UserHolder.setUser(UserSession.builder().id(USER_ID).build());
    }

    @AfterEach
    void tearDown() {
        UserHolder.clear();
    }

    // ==================== incrViewCount ====================

    @Test
    @DisplayName("incrViewCount: 首次浏览（setIfAbsent=true）-> 自增浏览量")
    void incrViewCount_firstView_increments() {
        String limitKey = PostConstants.Cache.VIEW_LIMIT_PREFIX + POST_ID + ":u:" + USER_ID;
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.setIfAbsent(limitKey, "1", 1, TimeUnit.DAYS)).thenReturn(Boolean.TRUE);
        when(redisTemplate.opsForHash()).thenReturn(hashOps);

        ops.incrViewCount(POST_ID);

        verify(hashOps).increment(postKey, CacheFieldConstants.VIEW_COUNT, 1L);
    }

    @Test
    @DisplayName("incrViewCount: 24h 内重复浏览（setIfAbsent=false）-> 不自增")
    void incrViewCount_duplicate_noneIncrement() {
        String limitKey = PostConstants.Cache.VIEW_LIMIT_PREFIX + POST_ID + ":u:" + USER_ID;
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.setIfAbsent(limitKey, "1", 1, TimeUnit.DAYS)).thenReturn(Boolean.FALSE);

        ops.incrViewCount(POST_ID);

        verify(redisTemplate, never()).opsForHash();
    }

    @Test
    @DisplayName("incrViewCount: Redis 异常 -> 吞掉不外抛")
    void incrViewCount_exception_swallowed() {
        when(redisTemplate.opsForValue()).thenThrow(new RuntimeException("redis down"));

        ops.incrViewCount(POST_ID);
    }

    // ==================== incrReplyCount / decrReplyCount ====================

    @Test
    @DisplayName("incrReplyCount: 回复数 +1")
    void incrReplyCount_increments() {
        when(redisTemplate.opsForHash()).thenReturn(hashOps);

        ops.incrReplyCount(POST_ID);

        verify(hashOps).increment(postKey, CacheFieldConstants.REPLY_COUNT, 1L);
    }

    @Test
    @DisplayName("decrReplyCount: 回复数 -1")
    void decrReplyCount_decrements() {
        when(redisTemplate.opsForHash()).thenReturn(hashOps);

        ops.decrReplyCount(POST_ID);

        verify(hashOps).increment(postKey, CacheFieldConstants.REPLY_COUNT, -1L);
    }

    @Test
    @DisplayName("incrReplyCount: Redis 异常 -> 吞掉不外抛")
    void incrReplyCount_exception_swallowed() {
        when(redisTemplate.opsForHash()).thenThrow(new RuntimeException("redis down"));

        ops.incrReplyCount(POST_ID);
    }

    // ==================== batchGetCounts ====================

    @Test
    @DisplayName("batchGetCounts: null/空 -> 空 Map")
    void batchGetCounts_empty() {
        assertThat(ops.batchGetCounts(null, CacheFieldConstants.VIEW_COUNT)).isEmpty();
        assertThat(ops.batchGetCounts(Collections.emptyList(), CacheFieldConstants.VIEW_COUNT)).isEmpty();
    }

    @Test
    @DisplayName("batchGetCounts: 正常解析 Pipeline 结果")
    void batchGetCounts_normal() {
        when(redisTemplate.executePipelined(any(SessionCallback.class)))
                .thenReturn(Arrays.<Object>asList(2L, "3"));

        Map<Long, Long> result = ops.batchGetCounts(List.of(1L, 2L), CacheFieldConstants.VIEW_COUNT);

        assertThat(result).containsEntry(1L, 2L).containsEntry(2L, 3L);
    }

    @Test
    @DisplayName("batchGetCounts: Pipeline 异常 -> 返回空 Map")
    void batchGetCounts_exception() {
        when(redisTemplate.executePipelined(any(SessionCallback.class)))
                .thenThrow(new RuntimeException("pipeline error"));

        assertThat(ops.batchGetCounts(List.of(1L), CacheFieldConstants.VIEW_COUNT)).isEmpty();
    }

    // ==================== batchGetLikeAndCollectCounts ====================

    @Test
    @DisplayName("batchGetLikeAndCollectCounts: null/空 -> 空 Map")
    void batchGetLikeAndCollect_empty() {
        assertThat(ops.batchGetLikeAndCollectCounts(null)).isEmpty();
        assertThat(ops.batchGetLikeAndCollectCounts(Collections.emptyList())).isEmpty();
    }

    @Test
    @DisplayName("batchGetLikeAndCollectCounts: 正常解析点赞/收藏数")
    void batchGetLikeAndCollect_normal() {
        when(redisTemplate.executePipelined(any(SessionCallback.class)))
                .thenReturn(Arrays.<Object>asList(Arrays.<Object>asList("4", "2")));

        Map<Long, Map<String, Long>> result = ops.batchGetLikeAndCollectCounts(List.of(1L));

        assertThat(result.get(1L))
                .containsEntry(CacheFieldConstants.LIKE_COUNT, 4L)
                .containsEntry(CacheFieldConstants.COLLECT_COUNT, 2L);
    }

    @Test
    @DisplayName("batchGetLikeAndCollectCounts: Pipeline 异常 -> 单条兜底读取")
    void batchGetLikeAndCollect_fallback() {
        when(redisTemplate.executePipelined(any(SessionCallback.class)))
                .thenThrow(new RuntimeException("pipeline error"));
        when(redisTemplate.opsForHash()).thenReturn(hashOps);
        when(hashOps.multiGet(eq(postKey), anyList()))
                .thenReturn(Arrays.<Object>asList("6", "1"));

        Map<Long, Map<String, Long>> result = ops.batchGetLikeAndCollectCounts(List.of(POST_ID));

        assertThat(result.get(POST_ID))
                .containsEntry(CacheFieldConstants.LIKE_COUNT, 6L)
                .containsEntry(CacheFieldConstants.COLLECT_COUNT, 1L);
    }
}
