package com.summit.stp.admin.application.command;

import com.summit.stp.activity.domain.model.CouponActivity;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class CreateCouponActivityCommand {
    private final Long couponId;
    private final String name;
    private Integer stock;
    private LocalDateTime activityStartTime;
    private  LocalDateTime activityEndTime;
    private Integer status;
    private final Integer type;
    private final Integer limitQuantity;
}
