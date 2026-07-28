package com.summit.stp.order.application.service;

import com.summit.stp.common.application.domain.event.OrderPaidEvent;

public interface EventPublishProvider {
    void publish(OrderPaidEvent orderPaidEvent);
}
