package com.summit.stp.order.order.application.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单模块通用的商品信息承载对象（用于防腐隔离）
 */
@Data
@Builder
public class ProductVO {
    /**
     * 商品/套餐ID
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    /**
     * 商品/套餐名称
     */
    private String name;

    /**
     * 商品/套餐单价
     */
    private BigDecimal price;

    /**
     * 商品/套餐折扣率
     */
    private BigDecimal discount;
}
