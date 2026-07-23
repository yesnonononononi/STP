package com.summit.stp.order.application.service;

import com.summit.stp.shared.domain.event.OrderPaidEvent;

public interface EventPublishProvider {
    void publish(OrderPaidEvent orderPaidEvent);
}
