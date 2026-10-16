package com.summit.stp.activity.application.service.impl;

import com.summit.stp.activity.domain.model.CouponActivity;
import com.summit.stp.activity.domain.repository.CouponActivityRepository;
import com.summit.stp.common.application.domain.exception.BusinessException;
import com.summit.stp.common.application.domain.exception.DisTributeLockAcquireException;
import com.summit.stp.common.application.domain.model.UserSession;
import com.summit.stp.common.auth.UserHolder;
import com.summit.stp.common.util.DistributedLockUtil;
import com.summit.stp.coupon.infrastructure.constants.CouponConstants;
import com.summit.stp.user_coupon.domain.repository.UserCouponRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.data.redis.connection.RedisHashCommands;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.types.Expiration;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link CouponActivityCacheProviderImpl} 的纯单元测试（Mockito，不依赖任何中间件）。
 *
 * <p>重点覆盖本次并发修复的 {@code cacheUserLimit}（原子 set-if-absent）以及
 * {@code deductStock} / {@code increaseStock} / {@code preWarmStock} /
 * {@code batchGetUserReceivedCounts} 的分支逻辑，为 PIT 变异测试提供有杀伤力的断言。</p>
 */
class CouponActivityCacheProviderImplTest {

    private static final long USER_ID = 7L;

    private static final long COUPON_ID = 9_000_001L;

    private StringRedisTemplate stringRedisTemplate;

    private DefaultRedisScript<Long> couponDeductStockScript;

    private DefaultRedisScript<Long> rollbackCouponScript;

    private RedisTemplate<String, Object> redisTemplate;

    private CouponActivityRepository couponActivityRepository;

    private UserCouponRepository userCouponRepository;

    private DistributedLockUtil distributedLockUtil;

    private HashOperations<String, Object, Object> hashOps;

    private CouponActivityCacheProviderImpl provider;

    private String userLimitKey;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        stringRedisTemplate = mock(StringRedisTemplate.class);
        couponDeductStockScript = mock(DefaultRedisScript.class);
        rollbackCouponScript = mock(DefaultRedisScript.class);
        redisTemplate = mock(RedisTemplate.class);
        couponActivityRepository = mock(CouponActivityRepository.class);
        userCouponRepository = mock(UserCouponRepository.class);
        distributedLockUtil = mock(DistributedLockUtil.class);
        hashOps = mock(HashOperations.class);
        userLimitKey = CouponConstants.Cache.USER_LIMITED_HASH + USER_ID;
        provider = new CouponActivityCacheProviderImpl(stringRedisTemplate, couponDeductStockScript,
                rollbackCouponScript, redisTemplate, couponActivityRepository,
                userCouponRepository, distributedLockUtil);
    }

    @AfterEach
    void tearDown() {
        UserHolder.clear();
    }

    // ==================== cacheUserLimit ====================

    @Test
    @DisplayName("cacheUserLimit: field 不存在 -> putIfAbsent 写入 '0' 并设置 TTL")
    void cacheUserLimit_newField_putsAndExpires() {
        when(stringRedisTemplate.opsForHash()).thenReturn(hashOps);
        when(hashOps.putIfAbsent(userLimitKey, Long.toString(COUPON_ID), "0")).thenReturn(Boolean.TRUE);

        provider.cacheUserLimit(USER_ID, COUPON_ID, 60_000L);

        verify(hashOps).putIfAbsent(userLimitKey, Long.toString(COUPON_ID), "0");
        verify(stringRedisTemplate).expire(userLimitKey, Duration.ofMillis(60_000L));
    }

    @Test
    @DisplayName("cacheUserLimit: field 已存在(putIfAbsent=false) -> 不覆盖、不设 TTL")
    void cacheUserLimit_existingField_noExpire() {
        when(stringRedisTemplate.opsForHash()).thenReturn(hashOps);
        when(hashOps.putIfAbsent(userLimitKey, Long.toString(COUPON_ID), "0")).thenReturn(Boolean.FALSE);

        provider.cacheUserLimit(USER_ID, COUPON_ID, 60_000L);

        verify(hashOps).putIfAbsent(userLimitKey, Long.toString(COUPON_ID), "0");
        verify(stringRedisTemplate, never()).expire(anyString(), any(Duration.class));
    }

    @Test
    @DisplayName("cacheUserLimit: putIfAbsent 返回 null -> 不设 TTL")
    void cacheUserLimit_putIfAbsentNull_noExpire() {
        when(stringRedisTemplate.opsForHash()).thenReturn(hashOps);
        when(hashOps.putIfAbsent(userLimitKey, Long.toString(COUPON_ID), "0")).thenReturn(null);

        provider.cacheUserLimit(USER_ID, COUPON_ID, 60_000L);

        verify(stringRedisTemplate, never()).expire(anyString(), any(Duration.class));
    }

    @Test
    @DisplayName("cacheUserLimit: duration 非正 -> 不设 TTL")
    void cacheUserLimit_nonPositiveDuration_noExpire() {
        when(stringRedisTemplate.opsForHash()).thenReturn(hashOps);
        when(hashOps.putIfAbsent(userLimitKey, Long.toString(COUPON_ID), "0")).thenReturn(Boolean.TRUE);

        provider.cacheUserLimit(USER_ID, COUPON_ID, 0L);

        verify(stringRedisTemplate, never()).expire(anyString(), any(Duration.class));
    }

    @Test
    @DisplayName("cacheUserLimit: userId 为 null -> 直接返回")
    void cacheUserLimit_nullUserId_returns() {
        provider.cacheUserLimit(null, COUPON_ID, 1000L);

        verifyNoInteractions(stringRedisTemplate);
    }

    @Test
    @DisplayName("cacheUserLimit: couponId 为 null -> 直接返回")
    void cacheUserLimit_nullCouponId_returns() {
        provider.cacheUserLimit(USER_ID, null, 1000L);

        verifyNoInteractions(stringRedisTemplate);
    }

    // ==================== increaseStock ====================

    @Test
    @DisplayName("increaseStock: 脚本返回 1 -> true")
    void increaseStock_returnsOne_true() {
        doReturn(1L).when(stringRedisTemplate).execute(eq(rollbackCouponScript), anyList(), any(), any(), any());

        assertThat(provider.increaseStock(1L, 2L, 3L)).isTrue();
    }

    @Test
    @DisplayName("increaseStock: 脚本返回 null -> false")
    void increaseStock_returnsNull_false() {
        doReturn(null).when(stringRedisTemplate).execute(eq(rollbackCouponScript), anyList(), any(), any(), any());

        assertThat(provider.increaseStock(1L, 2L, 3L)).isFalse();
    }

    @Test
    @DisplayName("increaseStock: 脚本返回其他值 -> false")
    void increaseStock_returnsOther_false() {
        doReturn(2L).when(stringRedisTemplate).execute(eq(rollbackCouponScript), anyList(), any(), any(), any());

        assertThat(provider.increaseStock(1L, 2L, 3L)).isFalse();
    }

    // ==================== deductStock ====================

    @Test
    @DisplayName("deductStock: -1 -> 库存不足异常")
    void deductStock_minusOne_throwsStock() {
        UserHolder.setUser(UserSession.builder().id(USER_ID).build());
        doReturn(-1L).when(stringRedisTemplate).execute(eq(couponDeductStockScript), anyList(), any(), any());

        assertThatThrownBy(() -> provider.deductStock(1L, 2L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("库存不足");
    }

    @Test
    @DisplayName("deductStock: -2 -> 已达限额异常")
    void deductStock_minusTwo_throwsLimit() {
        UserHolder.setUser(UserSession.builder().id(USER_ID).build());
        doReturn(-2L).when(stringRedisTemplate).execute(eq(couponDeductStockScript), anyList(), any(), any());

        assertThatThrownBy(() -> provider.deductStock(1L, 2L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户已到达最大领取限额");
    }

    @Test
    @DisplayName("deductStock: 0 -> true")
    void deductStock_zero_true() {
        UserHolder.setUser(UserSession.builder().id(USER_ID).build());
        doReturn(0L).when(stringRedisTemplate).execute(eq(couponDeductStockScript), anyList(), any(), any());

        assertThat(provider.deductStock(1L, 2L)).isTrue();
    }

    @Test
    @DisplayName("deductStock: 其他正值 -> false")
    void deductStock_other_false() {
        UserHolder.setUser(UserSession.builder().id(USER_ID).build());
        doReturn(5L).when(stringRedisTemplate).execute(eq(couponDeductStockScript), anyList(), any(), any());

        assertThat(provider.deductStock(1L, 2L)).isFalse();
    }

    @Test
    @DisplayName("deductStock: -3 -> 取锁并预热库存")
    void deductStock_minusThree_prewarmsStock() {
        UserHolder.setUser(UserSession.builder().id(USER_ID).build());
        doReturn(-3L).when(stringRedisTemplate).execute(eq(couponDeductStockScript), anyList(), any(), any());
        CouponActivity activity = activity(2L, 1L, 10);
        when(couponActivityRepository.findById(2L)).thenReturn(Optional.of(activity));
        doAnswer(invocation -> {
            ((Runnable) invocation.getArgument(1)).run();
            return null;
        }).when(distributedLockUtil).executeWithLock(anyString(), any(Runnable.class));
        when(stringRedisTemplate.opsForHash()).thenReturn(hashOps);

        assertThat(provider.deductStock(1L, 2L)).isTrue();
        assertThat(activity.getStock()).isEqualTo(9);
        verify(hashOps).putAndExpire(eq(CouponConstants.Cache.STOCK), eq(Map.of("2", "9")),
                eq(RedisHashCommands.HashFieldSetOption.UPSERT), any(Expiration.class));
    }

    @Test
    @DisplayName("deductStock: -3 且未获取到锁 -> 仍返回 true")
    void deductStock_minusThree_lockFailed_true() {
        UserHolder.setUser(UserSession.builder().id(USER_ID).build());
        doReturn(-3L).when(stringRedisTemplate).execute(eq(couponDeductStockScript), anyList(), any(), any());
        doThrow(new DisTributeLockAcquireException("busy"))
                .when(distributedLockUtil).executeWithLock(anyString(), any(Runnable.class));

        assertThat(provider.deductStock(1L, 2L)).isTrue();
    }

    // ==================== preWarmStock ====================

    @Test
    @DisplayName("preWarmStock: 活动已结束 -> 抛异常")
    void preWarmStock_ended_throws() {
        CouponActivity activity = CouponActivity.builder()
                .id(2L).couponId(1L).stock(10)
                .activityEndTime(LocalDateTime.now().minusMinutes(1))
                .build();

        assertThatThrownBy(() -> provider.preWarmStock(activity))
                .isInstanceOf(BusinessException.class)
                .hasMessage("活动已结束");
    }

    @Test
    @DisplayName("preWarmStock: 正常活动 -> 写入库存并设置 TTL")
    void preWarmStock_normal_writesStock() {
        when(stringRedisTemplate.opsForHash()).thenReturn(hashOps);

        provider.preWarmStock(activity(2L, 1L, 10));

        verify(hashOps).putAndExpire(eq(CouponConstants.Cache.STOCK), eq(Map.of("2", "10")),
                eq(RedisHashCommands.HashFieldSetOption.UPSERT), any(Expiration.class));
    }

    // ==================== batchGetUserReceivedCounts ====================

    @Test
    @DisplayName("batchGetUserReceivedCounts: userId 为 null -> 空 Map")
    void batchGet_nullUser_empty() {
        assertThat(provider.batchGetUserReceivedCounts(null, List.of(1L), 100L)).isEmpty();
    }

    @Test
    @DisplayName("batchGetUserReceivedCounts: couponIds 为空 -> 空 Map")
    void batchGet_emptyIds_empty() {
        assertThat(provider.batchGetUserReceivedCounts(USER_ID, List.of(), 100L)).isEmpty();
        assertThat(provider.batchGetUserReceivedCounts(USER_ID, null, 100L)).isEmpty();
    }

    @Test
    @DisplayName("batchGetUserReceivedCounts: 全部命中缓存 -> 直接返回")
    void batchGet_allCached() {
        List<Object> twoFields = Arrays.<Object>asList("1", "2");
        when(stringRedisTemplate.opsForHash()).thenReturn(hashOps);
        when(hashOps.multiGet(userLimitKey, twoFields)).thenReturn(Arrays.<Object>asList("3", "0"));

        Map<Long, Integer> result = provider.batchGetUserReceivedCounts(USER_ID, List.of(1L, 2L), 100L);

        assertThat(result).containsEntry(1L, 3).containsEntry(2L, 0);
    }

    @Test
    @DisplayName("batchGetUserReceivedCounts: 部分缺失 -> 加锁重建并对齐")
    void batchGet_missing_rebuildsFromDb() {
        List<Object> twoFields = Arrays.<Object>asList("1", "2");
        List<Object> oneField = Arrays.<Object>asList("1");
        when(stringRedisTemplate.opsForHash()).thenReturn(hashOps);
        when(hashOps.multiGet(userLimitKey, twoFields)).thenReturn(Arrays.<Object>asList(null, "4"));
        when(hashOps.multiGet(userLimitKey, oneField)).thenReturn(Arrays.<Object>asList((Object) null));
        when(userCouponRepository.countByUserIdAndCouponIds(eq(USER_ID), anyList())).thenReturn(Map.of(1L, 5));
        doAnswer(invocation -> {
            ((Runnable) invocation.getArgument(1)).run();
            return null;
        }).when(distributedLockUtil).executeWithLock(anyString(), any(Runnable.class));

        Map<Long, Integer> result = provider.batchGetUserReceivedCounts(USER_ID, List.of(1L, 2L), 100L);

        assertThat(result).containsEntry(1L, 5).containsEntry(2L, 4);
        verify(hashOps).putAndExpire(eq(userLimitKey), eq(Map.of("1", "5")),
                eq(RedisHashCommands.HashFieldSetOption.UPSERT), any(Expiration.class));
    }

    @Test
    @DisplayName("batchGetUserReceivedCounts: 获取重建锁失败 -> 降级补齐")
    void batchGet_lockFailed_fallbackFromDb() {
        List<Object> twoFields = Arrays.<Object>asList("1", "2");
        when(stringRedisTemplate.opsForHash()).thenReturn(hashOps);
        when(hashOps.multiGet(userLimitKey, twoFields)).thenReturn(Arrays.<Object>asList(null, null));
        doThrow(new DisTributeLockAcquireException("busy"))
                .when(distributedLockUtil).executeWithLock(anyString(), any(Runnable.class));
        Map<Long, Integer> dbCounts = new HashMap<>();
        dbCounts.put(1L, 1);
        dbCounts.put(2L, 2);
        when(userCouponRepository.countByUserIdAndCouponIds(eq(USER_ID), anyList())).thenReturn(dbCounts);

        Map<Long, Integer> result = provider.batchGetUserReceivedCounts(USER_ID, List.of(1L, 2L), 100L);

        assertThat(result).containsEntry(1L, 1).containsEntry(2L, 2);
    }

    @Test
    @DisplayName("batchGetUserReceivedCounts: Redis 异常 -> 直接回源数据库")
    void batchGet_redisDown_fallbackFromDb() {
        when(stringRedisTemplate.opsForHash()).thenThrow(new RuntimeException("redis down"));
        when(userCouponRepository.countByUserIdAndCouponIds(eq(USER_ID), ArgumentMatchers.<Long>anyList()))
                .thenReturn(Map.of(1L, 9));

        Map<Long, Integer> result = provider.batchGetUserReceivedCounts(USER_ID, List.of(1L), 100L);

        assertThat(result).containsEntry(1L, 9);
    }

    // ==================== getStockOfActivities ====================

    @Test
    @DisplayName("getStockOfActivities: 返回活动库存 Hash")
    void getStockOfActivities_returnsEntries() {
        Map<Object, Object> entries = new HashMap<>();
        entries.put("1", 10);
        when(redisTemplate.opsForHash()).thenReturn(hashOps);
        when(hashOps.entries(CouponConstants.Cache.ACTIVITY)).thenReturn(entries);

        assertThat(provider.getStockOfActivities()).containsEntry("1", 10);
    }

    // ==================== helpers ====================

    private CouponActivity activity(long activityId, long couponId, int stock) {
        LocalDateTime now = LocalDateTime.now();
        return CouponActivity.builder()
                .id(activityId)
                .couponId(couponId)
                .name("UT-活动")
                .stock(stock)
                .type(CouponActivity.Type.SECKILL)
                .status(1)
                .activityStartTime(now.minusMinutes(1))
                .activityEndTime(now.plusHours(1))
                .createTime(now)
                .build();
    }
}
