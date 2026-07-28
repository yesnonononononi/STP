package com.summit.stp.coupon.domain.service;

import com.summit.stp.coupon.domain.model.CouponActivity;

import java.util.List;
import java.util.Map;

public interface CouponCacheProvider {
    /**
     * 优惠券库存
     * @param couponId 优惠券id
     * @param limitCount 单人可领取优惠券上限
     * @param activityId 活动id
     * @return 是否成功
     */
    boolean deductStock(Long couponId,Long limitCount,Long activityId);
    /**
     * 优惠券库存回滚
     * @param couponId 优惠券id
     * @param uid 用户id
     * @param activityId 活动id
     * @return 是否成功
     */
    boolean increaseStock(Long couponId,Long uid,Long activityId);
    /**
     * 预热优惠券库存
     * @param activity 活动
     */
    void preWarmStock(CouponActivity activity);

    void cacheUserLimit(Long userId, Long couponId, Long duration);

    Map<Object, Object> getStockOfActivities();


}
