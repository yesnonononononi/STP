package com.summit.stp.common.application.domain.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 支付成功事件 - 跨服务共享的 MQ 事件契约
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaySuccessEvent {
    private Long orderId;
}
