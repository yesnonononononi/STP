package com.summit.stp.coupon.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 优惠券 (领域模型)
 */
@Getter
@EqualsAndHashCode
@Builder
public class Coupon {
    private final Long id;
    private final String name;
    private final BigDecimal discount;
    private  Integer stock;
    private final BigDecimal amount;
    private  int status;

    private Coupon(Long id, String name, BigDecimal discount, Integer stock, BigDecimal amount, int status) {
        this.id = id;
        this.name = name;
        this.discount = discount;
        this.stock = stock;
        this.amount = amount;
        this.status = status;
    }


    /**
     * 减少优惠券库存
     */
    public void deductStock(){
        this.stock -= 1;
    }

    /**
     * 增加优惠券模板库存
     */
    public void increaseStock() {
        if (this.stock != null) {
            this.stock += 1;
        }
    }

    /**
     * 禁用优惠券
     */
    public void ban(){
        this.status = 0;
    }

    /**
     * 启用优惠券
     */
    public void active(){
        this.status = 1;
    }
}
