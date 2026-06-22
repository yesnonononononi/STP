package com.summit.stp.order.application.service.impl;

import com.summit.stp.member.application.service.MemberMessageSender;
import com.summit.stp.order.domain.event.OrderPaidEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class EventPublishProvider implements com.summit.stp.order.application.service.EventPublishProvider {
    private final MemberMessageSender memberMessageSender;

    /**
     * 在事务提交后发布订单支付事件。
     * 使用 TransactionSynchronizationManager 确保消息发送仅在数据库事务成功提交后执行，
     * 避免事务回滚时消息已发送导致的数据不一致问题。
     *
     * @param orderPaidEvent 订单支付事件对象
     */
    @Override
    public void publish(OrderPaidEvent orderPaidEvent) {
        // 检查当前是否存在活跃的事务
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    // 事务提交成功后，发送消息到 RabbitMQ
                    memberMessageSender.sendMemberPay(orderPaidEvent);
                }
            });
        } else {
            // 如果没有活跃事务，直接发送消息（视业务需求而定，通常建议在有事务上下文时调用此方法）
            memberMessageSender.sendMemberPay(orderPaidEvent);
        }
    }
}
