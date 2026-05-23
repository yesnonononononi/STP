package com.summit.stp.coupon.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("coupon")
public class CouponPO {
    /**
     * 优惠券ID
     */
    @TableId(type = IdType.AUTO)
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
    /**
     * 库存
     */
    private Integer stock;

    private java.sql.Timestamp createTime;
    private java.sql.Timestamp updateTime;
}
