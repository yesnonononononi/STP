package com.summit.stp.payment.infrastructure.persistence.po;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CouponPO {
    /**
     * 优惠券ID
     */

    private Long id;
    /**
     * 优惠券名称
     */
    private String name;
    /**
     * 优惠折扣
     */
    private BigDecimal discount;
    /**
     * 优惠金额
     */
    private BigDecimal amount;
    /**
     * 优惠券类型
     */
    private Integer type;
    /**
     * 优惠券状态
     */
    private Integer status;

    private java.sql.Timestamp createTime;
    private java.sql.Timestamp updateTime;
}
