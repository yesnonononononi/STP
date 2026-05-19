package com.summit.stp.payment.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 优惠券 (领域模型)
 */
@Getter
@EqualsAndHashCode
public class Coupon {
    private final Long id;
    private final String name;
    private final BigDecimal discount;
    private final BigDecimal amount;
    private final int status;

    public Coupon(Long id, String name, BigDecimal discount, BigDecimal amount, int status) {
        this.id = id;
        this.name = name;
        this.discount = discount;
        this.amount = amount;
        this.status = status;
    }

    /**
     * 静态工厂方法
     */
    public static Coupon of(Long id, String name, BigDecimal amount, BigDecimal discount, Integer status) {
        return new Coupon(
                id,
                name,
                discount,
                amount,
                status != null ? status : 0);
    }

    public boolean isAvailable() {
        return status == 1;
    }
}
