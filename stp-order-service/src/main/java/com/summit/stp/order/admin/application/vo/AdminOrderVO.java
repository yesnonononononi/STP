package com.summit.stp.order.admin.application.vo;


import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;
@Data
@Builder
public class AdminOrderVO {
    private final Long id;          // 订单唯一ID (聚合根标识)
    private final Long creatorId;
    private final BigDecimal amount; // 支付金额
    private final Integer payType;   // 支付渠道类型
    private final String to;         // 收款方账户/地址（管理端已脱敏）
    // 说明：支付签名 sign 属敏感数据，管理端响应不再回传该字段（#21）
    private Integer status;      // 订单状态
    private final Long packageId;
    private final Integer quantity;
    private final Long couponId;
    private final BigDecimal unitPrice;
    private final BigDecimal discountAmount;
    private final Timestamp createTime;
    private final Timestamp timeoutTime;
    private Timestamp updateTime;
    private Timestamp payTime;
}
