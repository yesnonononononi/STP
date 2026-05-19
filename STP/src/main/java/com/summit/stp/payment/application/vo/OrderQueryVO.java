package com.summit.stp.payment.application.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 订单查询响应 VO (位于 Application 层，表示查询返回契约)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderQueryVO {
    private Long orderId;
    private BigDecimal amount;
    private String statusText;
    private String payTypeName;
    private Timestamp createTime;
    private String toName;
}
