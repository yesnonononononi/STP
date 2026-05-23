package com.summit.stp.order.infrastructure.listener;

import com.summit.stp.order.application.service.OrderAppService;
import com.summit.stp.payment.domain.event.PaySuccessEvent;
import com.summit.stp.shared.service.subcribe.domain.event.EventBus;
import com.summit.stp.shared.service.subcribe.domain.event.EventListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaySuccessListener implements EventListener<PaySuccessEvent> {

    private final OrderAppService orderAppService;

    @Override
    public void onEvent(PaySuccessEvent event, EventBus eventBus) {
        log.info("【订单支付成功监听】监听到支付成功事件，订单ID: {}", event.getOrderId());
        try {
            orderAppService.ackOrder(event.getOrderId());
        } catch (Exception e) {
            log.error("【订单支付成功监听】订单状态更新异常，订单ID: {}", event.getOrderId(), e);
        }
    }
}
