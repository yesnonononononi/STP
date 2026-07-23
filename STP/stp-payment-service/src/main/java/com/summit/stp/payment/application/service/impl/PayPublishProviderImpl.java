package com.summit.stp.payment.application.service.impl;

import com.summit.stp.payment.application.service.PayEventPublishProvider;
import com.summit.stp.payment.application.service.PayMessageSender;
import com.summit.stp.shared.domain.event.PayFailEvent;
import com.summit.stp.shared.domain.event.PaySuccessEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class PayPublishProviderImpl implements PayEventPublishProvider {
    private final PayMessageSender payMessageSender;

    @Override
    public void publish(PaySuccessEvent event) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    payMessageSender.sendPaySuccess(event);
                }
            });

        }else{
            payMessageSender.sendPaySuccess(event);
        }
    }

    @Override
    public void publish(PayFailEvent event) {
        payMessageSender.sendPayFail(event);
    }
}
