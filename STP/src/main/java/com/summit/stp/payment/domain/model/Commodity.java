package com.summit.stp.payment.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 商品 (领域模型)
 */
@Getter
@EqualsAndHashCode
public class Commodity {
    private final Long id;     // 商品ID
    private final String name; // 商品名称
    private final BigDecimal price; // 商品价格

    public Commodity(Long id, String name, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.price = price;
        checkPrice(); // 创建时强制验证领域规则
    }

    public static Commodity of(Long commodityId, String name, BigDecimal price) {
        return new Commodity(commodityId, name, price);
    }

    public void checkPrice() {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("商品价格不能小于或等于0");
        }
    }
}
