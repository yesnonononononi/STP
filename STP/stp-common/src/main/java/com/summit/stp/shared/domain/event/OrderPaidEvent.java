package com.summit.stp.shared.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * 订单支付成功事件 - 跨服务共享的 MQ 事件契约
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPaidEvent {
    private long orderId;
    private long creatorId;
    private String payType;
    private Timestamp payTime;
    private long packageId;
    private int quantity;
}
