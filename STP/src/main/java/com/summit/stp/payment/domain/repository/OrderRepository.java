package com.summit.stp.payment.domain.repository;

import com.summit.stp.payment.domain.model.Order;

import java.util.List;

/**
 * 订单仓储接口
 */
public interface OrderRepository {
    Long generateOrderId();

    void save(Order order);

    List<Order> queryHistoryOrders(long page, long pageSize);

    Order findOrderById(Long orderId);

    void deleteById(Long orderId);
}
