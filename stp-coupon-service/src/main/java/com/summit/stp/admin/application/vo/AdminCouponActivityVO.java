package com.summit.stp.admin.application.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Builder
public class AdminCouponActivityVO {
    private final Long id;
    private final Long couponId;
    private final String name;
    private Integer stock;
    private LocalDateTime activityStartTime;
    private  LocalDateTime activityEndTime;
    private Integer status;
    private final Integer type;
    private final Integer limitQuantity;
}
