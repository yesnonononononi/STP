package com.summit.stp.common.application.domain.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 支付失败事件 - 跨服务共享的 MQ 事件契约
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayFailEvent {
    private Long orderId;
    private String reason;
}
