package com.summit.stp.activity.application.service;

import com.summit.stp.activity.application.vo.CouponActivityQueryVO;
import com.summit.stp.common.application.api.result.Result;

import java.util.List;

public interface CouponActivityAppService {
    /**
     * 根据范围类型查询所有可用的优惠券投放活动列表
     */
    List<CouponActivityQueryVO> queryAllActivitiesByScopeType(Integer scopeType);

    /**
     * 批量查询用户针对优惠券的已领数量 (防腐层驱动)
     */
    java.util.Map<Long, Integer> batchCheckUserReceivedCounts(Long userId, List<Long> couponIds, Long maxDurationMs);

    /**
     * 用户根据活动ID领取优惠券
     *
     * @return
     */
    Result<Void> receiveActivityCoupon(Long activityId);


}
