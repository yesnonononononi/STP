package com.summit.stp.order.infrastructure.listener;

import com.summit.stp.order.application.service.OrderAppService;
import com.summit.stp.shared.domain.event.PayFailEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Deprecated
public class OrderPayFailListener {

    private final OrderAppService orderAppService;

    public void onEvent(PayFailEvent event) {
        log.warn("【订单支付失败监听】监听到支付失败事件，订单ID: {}, 原因: {}", event.getOrderId(), event.getReason());
        try {
            orderAppService.cancelOrder(event.getOrderId());
        } catch (Exception e) {
            log.error("【订单支付失败监听】订单状态更新异常，订单ID: {}", event.getOrderId(), e);
        }
    }
}
