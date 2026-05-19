package com.summit.stp.payment.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@Builder
@Table(name = "payment_order")
public class OrderPO {
    private Long id;
    private BigDecimal price;
    private long creatorId;
    private Integer payType;
    private String toName;
    @TableField(exist = false)
    private String sign;
    private Integer status; // 0: 待支付, 1: 已支付, 2: 已取消
    private java.sql.Timestamp createTime;
    private java.sql.Timestamp updateTime;
}
