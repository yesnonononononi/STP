package com.summit.stp.payment.application.service.impl;

import com.summit.stp.payment.application.service.PayEventPublishProvider;
import com.summit.stp.payment.domain.event.PayFailEvent;
import com.summit.stp.payment.domain.event.PaySuccessEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class PayPublishProviderImpl implements PayEventPublishProvider {
    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publish(PaySuccessEvent event) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    rabbitTemplate.convertAndSend("pay.exchange", "pay.queue.success", event);
                }
            });

        }else{
            rabbitTemplate.convertAndSend("pay.exchange", "pay.queue.success", event);
        }
    }

    @Override
    public void publish(PayFailEvent event) {
        rabbitTemplate.convertAndSend("pay.exchange", "pay.queue.fail", event);
    }
}
