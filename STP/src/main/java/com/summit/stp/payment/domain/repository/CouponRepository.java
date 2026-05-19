package com.summit.stp.payment.domain.repository;

import com.summit.stp.payment.domain.model.Coupon;

/**
 * 优惠券模板仓储接口
 */
public interface CouponRepository {
    /**
     * 根据模板 ID 查询优惠券模板详情
     */
    Coupon findCouponById(Long couponId);
}
