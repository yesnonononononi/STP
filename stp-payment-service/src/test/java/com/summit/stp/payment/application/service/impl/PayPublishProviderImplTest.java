package com.summit.stp.payment.application.service.impl;

import com.summit.stp.common.application.domain.event.PayFailEvent;
import com.summit.stp.common.application.domain.event.PaySuccessEvent;
import com.summit.stp.payment.application.service.PayMessageSender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * PayPublishProviderImpl 纯单元测试：验证事务内外发送时机。
 */
@ExtendWith(MockitoExtension.class)
class PayPublishProviderImplTest {

    @Mock
    private PayMessageSender payMessageSender;

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    @DisplayName("无事务 -> 直接 sendPaySuccess")
    void publishSuccess_noTransaction() {
        PayPublishProviderImpl provider = new PayPublishProviderImpl(payMessageSender);
        PaySuccessEvent event = new PaySuccessEvent(123L);

        provider.publish(event);

        verify(payMessageSender).sendPaySuccess(event);
    }

    @Test
    @DisplayName("有事务 -> 提交后才 sendPaySuccess")
    void publishSuccess_withTransaction() {
        PayPublishProviderImpl provider = new PayPublishProviderImpl(payMessageSender);
        PaySuccessEvent event = new PaySuccessEvent(123L);
        TransactionSynchronizationManager.initSynchronization();
        try {
            provider.publish(event);

            // 注册阶段不发送
            verify(payMessageSender, never()).sendPaySuccess(event);

            // 模拟事务提交
            for (TransactionSynchronization sync : TransactionSynchronizationManager.getSynchronizations()) {
                sync.afterCommit();
            }
            verify(payMessageSender).sendPaySuccess(event);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    @DisplayName("PayFailEvent 无论是否有事务都立即 sendPayFail")
    void publishFail_alwaysSendsImmediately() {
        PayPublishProviderImpl provider = new PayPublishProviderImpl(payMessageSender);
        PayFailEvent event = new PayFailEvent(123L, "fail");
        TransactionSynchronizationManager.initSynchronization();
        try {
            provider.publish(event);
            verify(payMessageSender).sendPayFail(event);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }
}
