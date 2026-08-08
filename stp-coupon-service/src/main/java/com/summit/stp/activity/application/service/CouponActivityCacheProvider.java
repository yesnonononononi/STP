package com.summit.stp.activity.application.service;

import com.summit.stp.activity.domain.model.CouponActivity;

import java.util.Map;

public interface CouponActivityCacheProvider {
    /**
     * 优惠券库存扣减
     * @param couponId 优惠券id
     * @param limitCount 单人可领取优惠券上限
     * @param activityId 活动id
     * @return 是否成功
     */
    boolean deductStock(Long couponId, Long limitCount, Long activityId);

    /**
     * 优惠券库存回滚
     * @param couponId 优惠券id
     * @param uid 用户id
     * @param activityId 活动id
     * @return 是否成功
     */
    boolean increaseStock(Long couponId, Long uid, Long activityId);

    /**
     * 预热优惠券活动库存
     * @param activity 活动
     */
    void preWarmStock(CouponActivity activity);

    /**
     * 记录用户限领缓存
     */
    void cacheUserLimit(Long userId, Long couponId, Long duration);

    /**
     * 批量获取用户针对优惠券的已领数量 (防腐层：优先缓存，缺失则分布式锁重建，Redis宕机降级DB)
     *
     * @param userId          用户ID
     * @param couponIds       优惠券模板ID列表
     * @param defaultDuration 活动最长TTL（毫秒）
     * @return couponId -> 已领数量
     */
    Map<Long, Integer> batchGetUserReceivedCounts(Long userId, java.util.List<Long> couponIds, Long defaultDuration);

    Map<Object, Object> getStockOfActivities();
}
