package com.summit.stp.order.application.service.impl;

import com.summit.stp.common.constants.MqConstants;
import com.summit.stp.common.application.domain.event.OrderPaidEvent;
import com.summit.stp.common.application.service.queue.QueueSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class EventPublishProvider implements com.summit.stp.order.application.service.EventPublishProvider {
    private final QueueSender queueSender;

    /**
     * 在事务提交后发布订单支付事件。
     * 使用 TransactionSynchronizationManager 确保消息发送仅在数据库事务成功提交后执行，
     * 避免事务回滚时消息已发送导致的数据不一致问题。
     *
     * @param orderPaidEvent 订单支付事件对象
     */
    @Override
    public void publish(OrderPaidEvent orderPaidEvent) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    queueSender.send(MqConstants.Member.EXCHANGE, MqConstants.Member.ROUTING_KEY, orderPaidEvent);
                }
            });
        } else {
            queueSender.send(MqConstants.Member.EXCHANGE, MqConstants.Member.ROUTING_KEY, orderPaidEvent);
        }
    }
}
