package com.summit.stp.payment.application.service;

import com.summit.stp.common.application.service.queue.QueueSender;
import com.summit.stp.common.constants.MqConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PayMessageSender {
    private final QueueSender queueSender;

    /**
     * 发送支付成功事件
     */
    public void sendPaySuccess(Object event) {
        queueSender.send(MqConstants.Pay.EXCHANGE, MqConstants.Pay.ROUTING_KEY_SUCCESS, event);
    }

    /**
     * 发送支付失败事件
     */
    public void sendPayFail(Object event) {
        queueSender.send(MqConstants.Pay.EXCHANGE, MqConstants.Pay.ROUTING_KEY_FAIL, event);
    }
}
