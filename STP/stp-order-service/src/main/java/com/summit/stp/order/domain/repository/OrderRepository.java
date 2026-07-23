package com.summit.stp.order.domain.repository;

import com.summit.stp.shared.application.vo.OrderQueryVO;
import com.summit.stp.order.domain.model.Order;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 订单仓储接口
 */
public interface OrderRepository {
    Long generateOrderId();

    void save(Order order);

    List<Order> queryHistoryOrders(long page, long pageSize);

    Order findOrderById(Long orderId);

    void deleteById(Long orderId);

    void timeout(Long aLong);

    Map<Long, OrderQueryVO> findOrderByCouponIds(Long currentUserId, List<Long> ids);

    Map<Long,Order> findOrderByIds(Set<String> orders);

    void batchUpdate(List<Order> changedOrders);
}
