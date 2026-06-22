package com.summit.stp.coupon.application.service.impl;

import com.summit.stp.coupon.domain.model.CouponActivity;
import com.summit.stp.coupon.domain.service.CouponCacheProvider;

import com.summit.stp.shared.ThreadContext.UserHolder;
import com.summit.stp.shared.constants.RedisConstants;
import com.summit.stp.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisHashCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class CouponCacheProviderImpl implements CouponCacheProvider {
    private final StringRedisTemplate stringRedisTemplate;
    private final DefaultRedisScript<Long> couponDeductStockScript;
    private final DefaultRedisScript<Long> rollbackCouponScript;
    private final RedisTemplate<Object, Object> redisTemplate;


    @Override
    public boolean deductStock(Long couponId, Long limitCount, Long activityId) {
        Long uid = UserHolder.getUser().getId();
        int execute = Math.toIntExact(stringRedisTemplate.execute(
                couponDeductStockScript,
                Arrays.asList(RedisConstants.Coupon.STOCK, RedisConstants.Coupon.USER_LIMITED_HASH + uid),
                String.valueOf(limitCount),   // ARGV[1]
                String.valueOf(activityId),   // ARGV[2]
                String.valueOf(couponId)      // ARGV[3]
        ));
        switch (execute) {
            case (-1) -> throw new BusinessException("库存不足");

            case -2 -> throw new BusinessException("用户已到达最大领取限额");

            case 0 -> {
                return true;
            }
            default -> {
                return false;
            }
        }

    }

    @Override
    public boolean increaseStock(Long couponId, Long uid, Long activityId) {
        return stringRedisTemplate.execute(
                rollbackCouponScript,
                Arrays.asList(RedisConstants.Coupon.STOCK , RedisConstants.Coupon.USER_LIMITED_HASH ),
                Arrays.asList(String.valueOf(uid), String.valueOf(activityId), String.valueOf(couponId))
        ) == 1;
    }

    @Override
    public void preWarmStock(CouponActivity activity) {
        LocalDateTime now = LocalDateTime.now();
        if (activity.hasEnded(now)) {  // 活动结束
            throw new BusinessException("活动已结束");
        }
        String key = RedisConstants.Coupon.STOCK;
        long duration = activity.getDuration(now);
        if (duration <= 0) {
            log.warn("【活动预热】无效的活动持续时间:{} 活动: {} ", duration, activity.getId());
            throw new BusinessException("无效的活动持续时间");
        }
        stringRedisTemplate.opsForHash().putAndExpire(key, Map.of(activity.getId().toString(), activity.getStock().toString()), RedisHashCommands.HashFieldSetOption.UPSERT, Expiration.from(Duration.of(duration, ChronoUnit.MILLIS)));
    }

    @Override
    public void cacheUserLimit(Long userId, Long couponId, Long duration){
        String key = RedisConstants.Coupon.USER_LIMITED_HASH + couponId;
        if(stringRedisTemplate.hasKey(key))return;
        stringRedisTemplate.opsForHash().putAndExpire(key, Map.of(userId.toString(), "0"), RedisHashCommands.HashFieldSetOption.UPSERT, Expiration.from(Duration.of(duration, ChronoUnit.MILLIS)));
    }


    @Override
    public Map<Object, Object> getStockOfActivities() {
        String key = RedisConstants.Coupon.ACTIVITY;
        return redisTemplate.opsForHash().entries(key);
    }


}
