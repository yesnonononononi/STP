package com.summit.stp.coupon.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class CouponUseScope {
    private final Long id;
    private final Long couponId;
    private final Long relationId;
}
