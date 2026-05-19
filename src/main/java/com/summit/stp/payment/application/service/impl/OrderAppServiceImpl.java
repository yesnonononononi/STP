package com.summit.stp.payment.application.service.impl;

import com.summit.stp.payment.application.vo.OrderQueryVO;
import com.summit.stp.payment.application.service.OrderAppService;
import com.summit.stp.payment.domain.model.Order;
import com.summit.stp.payment.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单应用服务实现类
 * 直接依赖 OrderRepository 进行编排，彻底砍掉无业务逻辑的冗余领域服务 OrderService (符合 DDD)
 */
@Service
@RequiredArgsConstructor
public class OrderAppServiceImpl implements OrderAppService {
    private final OrderRepository orderRepository;

    @Override
    public OrderQueryVO queryById(Long orderId) {
        Order order = orderRepository.findOrderById(orderId);
        if (order == null) {
            return null;
        }
        return mapToQueryResponse(order);
    }

    @Override
    public void deleteById(Long orderId) {
        orderRepository.deleteById(orderId);
    }

    @Override
    public List<OrderQueryVO> queryHistory(long page, long pageSize) {
        List<Order> orders = orderRepository.queryHistoryOrders(page, pageSize);
        return orders.stream()
                .map(this::mapToQueryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void ackOrder(Long orderId) {
        // 1. [应用服务层] 校验订单是否存在
        Order order = orderRepository.findOrderById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在，ID: " + orderId);
        }

        // 2. [领域实体层] 校验订单是否已完成，并进行合法状态转移
        order.payComplete();

        // 3. [应用服务层协调] 将更新后的领域聚合根持久化存回数据库
        orderRepository.save(order);
    }

    private OrderQueryVO mapToQueryResponse(Order order) {
        return OrderQueryVO.builder()
                .orderId(order.getId())
                .amount(order.getAmount())
                .statusText(order.getStatus().getDescription())
                .payTypeName(order.getPayType() != null ? order.getPayType().getType() : "未知")
                .createTime(order.getCreateTime())
                .toName(order.getTo())
                .build();
    }
}
