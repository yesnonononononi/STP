package com.summit.stp.coupon.api.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券查询响应 VO - 跨服务共享的契约
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CouponQueryVO implements Serializable {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;
    private String name;
    private BigDecimal discount;
    private BigDecimal amount;
    private Integer status;
    private Integer type;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer validDays;
    private Integer validHours;
    private String image;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long relatedOrderId;
    /**
     * 优惠券关联的订单商品
     */
    private String orderCommodityName;
}

