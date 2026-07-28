package com.summit.stp.coupon.domain.repository;


import com.summit.stp.coupon.domain.model.CouponUseScope;


public interface CouponUseScopeRepository {
    CouponUseScope findByCouponId(Long couponId);
}
