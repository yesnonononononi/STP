package com.summit.stp.order.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("payment_order")
public class OrderPO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private BigDecimal amount;
    private long creatorId;
    private Integer payType;
    @TableField(exist = false)
    private String toName;
    @TableField(exist = false)
    private String sign;
    private Integer status; // 0: 待支付, 1: 已支付, 2: 已完成 3:已取消
    @TableField("package_id")
    private Long packageId;
    private Integer quantity;
    private Long couponId;
    private BigDecimal unitPrice;
    private BigDecimal discountAmount;
    private Timestamp createTime;
    private Timestamp updateTime;
    private Timestamp payTime;
}
