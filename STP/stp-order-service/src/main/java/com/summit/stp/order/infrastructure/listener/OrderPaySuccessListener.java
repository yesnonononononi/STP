package com.summit.stp.order.infrastructure.listener;

import com.summit.stp.order.application.service.OrderAppService;
import com.summit.stp.common.application.domain.event.PaySuccessEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Deprecated
public class OrderPaySuccessListener {

    private final OrderAppService orderAppService;

    public void onEvent(PaySuccessEvent event) {
        log.info("【订单支付成功监听】监听到支付成功事件，订单ID: {}", event.getOrderId());
        try {
            orderAppService.ackOrder(event.getOrderId());
        } catch (Exception e) {
            log.error("【订单支付成功监听】订单状态更新异常，订单ID: {}", event.getOrderId(), e);
        }
    }
}
