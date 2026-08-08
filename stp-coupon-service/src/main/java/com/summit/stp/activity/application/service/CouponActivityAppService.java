package com.summit.stp.activity.application.service;

import com.summit.stp.activity.application.dto.CouponActivityDTO;
import com.summit.stp.activity.application.vo.CouponActivityQueryVO;

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
     */
    void receiveActivityCoupon(Long activityId);

    /**
     * 新增优惠券投放活动
     */
    void saveActivity(CouponActivityDTO dto);

    /**
     * 更新优惠券投放活动
     */
    void updateActivity(CouponActivityDTO dto);

    /**
     * 删除指定的优惠券投放活动
     */
    void deleteActivity(Long id);
}
