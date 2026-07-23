package com.summit.stp.coupon.application.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CouponQueryVO {
    private Long id;
    private String name;
    private BigDecimal discount;
    private BigDecimal amount;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer validDays;
    private Integer validHours;
    private Integer scopeType;
    /**
     * 当前订单是否可用标识
     */
    private Boolean isAvailable;
    private String reason;
    private String description;
    private LocalDateTime createTime;
    /**
     * 优惠券关联的订单ID
     */
    private Long relatedOrderId;
    /**
     * 优惠券关联的订单商品
     */
    private String orderCommodityName;
}
