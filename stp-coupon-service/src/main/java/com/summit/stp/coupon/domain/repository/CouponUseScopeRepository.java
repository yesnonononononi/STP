package com.summit.stp.coupon.domain.repository;


import com.summit.stp.coupon.domain.model.CouponUseScope;

import java.util.List;
import java.util.Map;

public interface CouponUseScopeRepository {
    CouponUseScope findByCouponId(Long couponId);

    Map<Long, CouponUseScope> findByCouponIds(List<Long> couponIds);
}
