package com.summit.stp.order.application.service;

public interface OrderSafeService {
    void processOrderTimeout(Long duration, Long orderId);
    void removeOrderTimeout(Long orderId);
}
