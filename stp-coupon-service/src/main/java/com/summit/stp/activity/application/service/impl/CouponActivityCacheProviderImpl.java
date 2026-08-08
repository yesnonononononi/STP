package com.summit.stp.activity.application.service.impl;

import com.summit.stp.activity.application.service.CouponActivityCacheProvider;
import com.summit.stp.activity.domain.model.CouponActivity;
import com.summit.stp.activity.domain.repository.CouponActivityRepository;
import com.summit.stp.common.ThreadContext.UserHolder;
import com.summit.stp.common.application.domain.exception.BusinessException;
import com.summit.stp.common.application.domain.exception.DisTributeLockAcquireException;
import com.summit.stp.common.util.DistributedLockUtil;
import com.summit.stp.coupon.infrastructure.constants.CouponConstants;
import com.summit.stp.user_coupon.domain.repository.UserCouponRepository;
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
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponActivityCacheProviderImpl implements CouponActivityCacheProvider {
    private final StringRedisTemplate stringRedisTemplate;
    private final DefaultRedisScript<Long> couponDeductStockScript;
    private final DefaultRedisScript<Long> rollbackCouponScript;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CouponActivityRepository couponActivityRepository;
    private final UserCouponRepository userCouponRepository;
    private final DistributedLockUtil distributedLockUtil;

    @Override
    public boolean deductStock(Long couponId, Long limitCount, Long activityId) {
        Long uid = UserHolder.getUser().getId();
        int execute = Math.toIntExact(stringRedisTemplate.execute(
                couponDeductStockScript,
                Arrays.asList(CouponConstants.Cache.STOCK, CouponConstants.Cache.USER_LIMITED_HASH + uid),
                String.valueOf(limitCount),   // ARGV[1]
                String.valueOf(activityId),   // ARGV[2]
                String.valueOf(couponId)      // ARGV[3]
        ));
        switch (execute) {
            case (-1) -> throw new BusinessException("库存不足");
            case -2 -> throw new BusinessException("用户已到达最大领取限额");
            case -3 -> {
                try {
                    distributedLockUtil.executeWithLock(CouponConstants.Cache.PREWARM_LOCK + couponId + ":" + activityId, () -> {
                        CouponActivity byId = couponActivityRepository.findById(activityId);
                        if (byId != null) {
                            byId.deductStock();
                            this.preWarmStock(byId);
                        }
                    });
                } catch (DisTributeLockAcquireException e) {
                    return true;
                }
                return true;
            }
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
        Long res = stringRedisTemplate.execute(
                rollbackCouponScript,
                Arrays.asList(CouponConstants.Cache.STOCK, CouponConstants.Cache.USER_LIMITED_HASH),
                String.valueOf(uid),
                String.valueOf(activityId),
                String.valueOf(couponId)
        );
        return res != null && res == 1L;
    }

    @Override
    public void preWarmStock(CouponActivity activity) {
        LocalDateTime now = LocalDateTime.now();
        if (activity.hasEnded(now)) {
            throw new BusinessException("活动已结束");
        }
        String key = CouponConstants.Cache.STOCK;
        long duration = activity.getDuration(now);
        if (duration <= 0) {
            log.warn("【优惠券活动】活动持续时间无效:{} 活动ID:{}", duration, activity.getId());
            throw new BusinessException("无效的活动持续时间");
        }
        stringRedisTemplate.opsForHash().putAndExpire(
                key,
                Map.of(activity.getId().toString(), activity.getStock().toString()),
                RedisHashCommands.HashFieldSetOption.UPSERT,
                Expiration.from(Duration.of(duration, ChronoUnit.MILLIS))
        );
    }

    @Override
    public void cacheUserLimit(Long userId, Long couponId, Long duration) {
        String key = CouponConstants.Cache.USER_LIMITED_HASH + userId;
        Boolean hasField = stringRedisTemplate.opsForHash().hasKey(key, couponId.toString());
        if (hasField != null && hasField) {
            return;
        }
        stringRedisTemplate.opsForHash().putAndExpire(
                key,
                Map.of(couponId.toString(), "0"),
                RedisHashCommands.HashFieldSetOption.UPSERT,
                Expiration.from(Duration.of(duration, ChronoUnit.MILLIS))
        );
    }

    @Override
    public Map<Long, Integer> batchGetUserReceivedCounts(Long userId, List<Long> couponIds, Long defaultDuration) {
        if (userId == null || couponIds == null || couponIds.isEmpty()) {
            return Map.of();
        }
        String userLimitKey = CouponConstants.Cache.USER_LIMITED_HASH + userId;
        Map<Long, Integer> resultMap = new HashMap<>();
        List<Long> missingIds = new ArrayList<>();

        try {
            readFromCache(userLimitKey, couponIds, resultMap, missingIds);
            if (missingIds.isEmpty()) {
                return resultMap;
            }
            rebuildCacheWithLock(userId, userLimitKey, missingIds, resultMap, defaultDuration);
            return resultMap;
        } catch (Exception e) {
            return fallbackFromDb(userId, couponIds, e.getMessage());
        }
    }

    private void readFromCache(String userLimitKey, List<Long> couponIds,
                               Map<Long, Integer> resultMap, List<Long> missingIds) {
        List<Object> couponFields = couponIds.stream().map(Object::toString).collect(Collectors.toList());
        List<Object> cachedCounts = stringRedisTemplate.opsForHash().multiGet(userLimitKey, couponFields);

        for (int i = 0; i < couponIds.size(); i++) {
            Long couponId = couponIds.get(i);
            Object countObj = cachedCounts != null && i < cachedCounts.size() ? cachedCounts.get(i) : null;
            if (countObj != null) {
                try {
                    resultMap.put(couponId, Integer.parseInt(countObj.toString()));
                } catch (NumberFormatException e) {
                    missingIds.add(couponId);
                }
            } else {
                missingIds.add(couponId);
            }
        }
    }

    private void rebuildCacheWithLock(Long userId, String userLimitKey, List<Long> missingIds,
                                      Map<Long, Integer> resultMap, Long defaultDuration) {
        String lockKey = CouponConstants.Cache.USER_LIMITED_LOCK + userId;
        try {
            distributedLockUtil.executeWithLock(lockKey, () -> doRebuildAndAlign(userId, userLimitKey, missingIds, resultMap, defaultDuration));
        } catch (DisTributeLockAcquireException e) {
            log.warn("【优惠券活动】获取限领重建锁失败，降级补齐 用户ID:{}", userId);
            fillMissingFromDb(userId, missingIds, resultMap);
        }
    }

    private void doRebuildAndAlign(Long userId, String userLimitKey, List<Long> missingIds,
                                   Map<Long, Integer> resultMap, Long defaultDuration) {
        List<Long> stillMissing = filterStillMissing(userLimitKey, missingIds, resultMap);
        if (stillMissing.isEmpty()) {
            return;
        }

        Map<Long, Integer> dbCounts = userCouponRepository.countByUserIdAndCouponIds(userId, stillMissing);
        Map<String, String> cacheUpdates = new HashMap<>();
        for (Long cId : stillMissing) {
            int count = dbCounts.getOrDefault(cId, 0);
            resultMap.put(cId, count);
            cacheUpdates.put(cId.toString(), String.valueOf(count));
        }

        long ttl = (defaultDuration != null && defaultDuration > 0) ? defaultDuration : Duration.ofDays(7).toMillis();
        stringRedisTemplate.opsForHash().putAndExpire(
                userLimitKey,
                cacheUpdates,
                RedisHashCommands.HashFieldSetOption.UPSERT,
                Expiration.from(Duration.of(ttl, ChronoUnit.MILLIS))
        );
        log.info("【优惠券活动】用户限领缓存重建对齐成功 用户ID:{} 券模板数量:{}", userId, cacheUpdates.size());
    }

    private List<Long> filterStillMissing(String userLimitKey, List<Long> missingIds, Map<Long, Integer> resultMap) {
        List<Object> checkFields = missingIds.stream().map(Object::toString).collect(Collectors.toList());
        List<Object> doubleCheckCounts = stringRedisTemplate.opsForHash().multiGet(userLimitKey, checkFields);
        List<Long> stillMissing = new ArrayList<>();

        for (int j = 0; j < missingIds.size(); j++) {
            Long cId = missingIds.get(j);
            Object val = doubleCheckCounts != null && j < doubleCheckCounts.size() ? doubleCheckCounts.get(j) : null;
            if (val != null) {
                resultMap.put(cId, Integer.parseInt(val.toString()));
            } else {
                stillMissing.add(cId);
            }
        }
        return stillMissing;
    }

    private void fillMissingFromDb(Long userId, List<Long> missingIds, Map<Long, Integer> resultMap) {
        Map<Long, Integer> dbCounts = userCouponRepository.countByUserIdAndCouponIds(userId, missingIds);
        for (Long cId : missingIds) {
            resultMap.put(cId, dbCounts.getOrDefault(cId, 0));
        }
    }

    private Map<Long, Integer> fallbackFromDb(Long userId, Collection<Long> couponIds, String reason) {
        log.warn("【优惠券活动】Redis不可用，触发降级直接从数据库查询已领限额 用户ID:{} 异常:{}", userId, reason);
        return userCouponRepository.countByUserIdAndCouponIds(userId, couponIds);
    }

    @Override
    public Map<Object, Object> getStockOfActivities() {
        String key = CouponConstants.Cache.ACTIVITY;
        return redisTemplate.opsForHash().entries(key);
    }
}
