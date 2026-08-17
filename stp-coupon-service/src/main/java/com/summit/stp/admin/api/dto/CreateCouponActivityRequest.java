package com.summit.stp.admin.api.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateCouponActivityRequest {
    private final Long couponId;
    private final String name;
    private Integer stock;
    private LocalDateTime activityStartTime;
    private  LocalDateTime activityEndTime;
    private Integer status;
    private final Integer type;
    private final Integer limitQuantity;
}
