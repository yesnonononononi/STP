package com.summit.stp.payment.application.service;

import com.summit.stp.common.application.domain.event.PayFailEvent;
import com.summit.stp.common.application.domain.event.PaySuccessEvent;

public interface PayEventPublishProvider {
    void publish(PaySuccessEvent event);
    void publish(PayFailEvent event);
}
