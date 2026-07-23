package com.summit.stp.coupon.application.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponActivityQueryVO {
    private Long id;
    private Long couponId;
    private String name;
    private Integer stock;
    private LocalDateTime activityStartTime;
    private LocalDateTime activityEndTime;
    private Integer status;
    private Integer limitQuantity;
    private Integer type;
    // 关联的优惠券模板信息
    private String couponName;
    private BigDecimal discount;
    private BigDecimal amount;
    private Integer couponType; // 0折扣, 1金额
    private Integer scopeType; // 可用范围类型
    private String description; // 优惠券描述
    private Integer timeType;
    private String image; // 优惠券图片
    private Integer validDays;
    private Integer validHours;
    private String scopeDescription;
    private boolean isAvailable;
}
