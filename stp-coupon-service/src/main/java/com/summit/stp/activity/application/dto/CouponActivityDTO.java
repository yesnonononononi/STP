package com.summit.stp.activity.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 优惠券活动数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponActivityDTO {
    private Long id;
    private Long couponId;
    private String name;
    private Integer stock;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime activityStartTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime activityEndTime;

    private Integer status;
    private Integer limitQuantity;
    private Integer type;
}
