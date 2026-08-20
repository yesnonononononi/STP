package com.summit.stp.activity.domain.event;

import com.summit.stp.user_coupon.domain.model.UserCoupon;
import lombok.Builder;
import lombok.Data;


@Builder
@Data
public class SeckillEvent {
    private UserCoupon userCoupon;
}
