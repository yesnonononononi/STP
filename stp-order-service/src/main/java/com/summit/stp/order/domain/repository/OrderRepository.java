package com.summit.stp.order.domain.repository;

import com.summit.stp.common.application.api.vo.OrderQueryVO;
import com.summit.stp.order.domain.model.Order;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 订单仓储接口
 */
public interface OrderRepository {
    Long generateOrderId();

    void save(Order order);

    List<Order> queryHistoryOrders(long page, long pageSize);

    Order findOrderById(Long orderId);

    void deleteById(Long orderId);


    Map<Long, OrderQueryVO> findOrderByCouponIds(Long currentUserId, List<Long> ids);

    List<Order> findOrderByIds(Collection<Long> orderIds);

    /**
     * 批量查询待支付且已超过订单超时时间的订单。
     *
     * @param currentTime 当前时间
     * @param limit       本次最多查询数量
     * @return 已超时的待支付订单
     */
    List<Order> findPendingExpiredOrders(Timestamp currentTime, int limit);

    void batchUpdate(List<Order> changedOrders);
}
