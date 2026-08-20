package com.summit.stp.admin.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateCouponActivityRequest {
    private final Long id;
    private final Long couponId;
    private final String name;
    private Integer stock;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime activityStartTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime activityEndTime;

    private Integer status;
    private final Integer type;
}
