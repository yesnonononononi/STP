package com.summit.stp.coupon.domain.repository;

import com.summit.stp.coupon.domain.model.Coupon;

import java.util.List;
import java.util.Map;

/**
 * 优惠券模板仓储接口
 */
public interface CouponRepository {
    /**
     * 根据模板 ID 查询优惠券模板详情
     */
    Coupon findCouponById(Long couponId);

    void update(Coupon template);

    List<Coupon> findByIds(List<Long> cList);
}
