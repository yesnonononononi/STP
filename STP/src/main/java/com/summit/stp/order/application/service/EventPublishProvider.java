package com.summit.stp.order.application.service;

import com.summit.stp.order.domain.event.OrderPaidEvent;

public interface EventPublishProvider {
    void publish(OrderPaidEvent orderPaidEvent);
}
