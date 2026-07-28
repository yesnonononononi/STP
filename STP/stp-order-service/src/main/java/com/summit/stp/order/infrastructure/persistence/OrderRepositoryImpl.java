package com.summit.stp.order.infrastructure.persistence;

import cn.hutool.core.lang.generator.SnowflakeGenerator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.common.application.vo.OrderQueryVO;
import com.summit.stp.order.domain.model.Order;
import com.summit.stp.order.domain.model.OrderStatus;
import com.summit.stp.order.domain.repository.OrderRepository;
import com.summit.stp.order.infrastructure.persistence.mapper.OrderMapper;
import com.summit.stp.order.infrastructure.persistence.po.OrderPO;
import com.summit.stp.common.application.domain.model.PayType;
import com.summit.stp.common.ThreadContext.UserHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {
    private final OrderMapper orderMapper;

    @Override
    public Long generateOrderId() {
        return new SnowflakeGenerator().next();
    }

    @Override
    public void save(Order order) {
        OrderPO po = toPO(order);
        log.info("【保存订单】订单id:{}",order.getId());
        if (order.getStatus() == OrderStatus.PENDING) {
            orderMapper.insert(po);
        } else {
            orderMapper.updateById(po);
        }
    }

    @Override
    public List<Order> queryHistoryOrders(long page, long pageSize) {
        Long uid = UserHolder.getUser().getId();
        LambdaQueryWrapper<OrderPO> eq = new LambdaQueryWrapper<OrderPO>()
                .eq(OrderPO::getCreatorId, uid)
                .orderByDesc(OrderPO::getId);
        Page<OrderPO> orderPOPage = orderMapper.selectPage(new Page<>(page, pageSize), eq);
        List<OrderPO> records = orderPOPage.getRecords();
        if (records.isEmpty()) {
            return List.of();
        }

        return records.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Order findOrderById(Long orderId) {
        OrderPO po = orderMapper.selectById(orderId);
        if (po == null) {
            return null;
        }

        //查询套餐
        return toDomain(po);
    }

    @Override
    public void deleteById(Long orderId) {
        orderMapper.deleteById(orderId);
    }

    @Override
    public Map<Long, OrderQueryVO> findOrderByCouponIds(Long currentUserId, List<Long> ids) {
        LambdaQueryWrapper<OrderPO> wrapper = new LambdaQueryWrapper<OrderPO>().in(OrderPO::getCouponId, ids).eq(OrderPO::getCreatorId, currentUserId).in(OrderPO::getStatus, OrderStatus.PAID.getCode(), OrderStatus.COMPLETED.getCode());
        List<OrderPO> orderPOS = orderMapper.selectList(wrapper);
        return orderPOS.stream().collect(Collectors.toMap(OrderPO::getCouponId,po->
            OrderQueryVO.builder()
                    .orderId(po.getId())
                    .memberId(po.getPackageId())
                    .build()
        ));

    }

    @Override
    public List<Order> findOrderByIds(Collection<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return List.of();
        }
        return orderMapper.selectByIds(orderIds).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Order> findPendingExpiredOrders(Timestamp currentTime, int limit) {
        if (limit <= 0) {
            return List.of();
        }
        LambdaQueryWrapper<OrderPO> wrapper = new LambdaQueryWrapper<OrderPO>()
                .eq(OrderPO::getStatus, OrderStatus.PENDING.getCode())
                .le(OrderPO::getTimeoutTime, currentTime)
                .orderByAsc(OrderPO::getTimeoutTime)
                .orderByAsc(OrderPO::getId);
        Page<OrderPO> page = new Page<>(1, limit, false);
        return orderMapper.selectPage(page, wrapper).getRecords().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void batchUpdate(List<Order> changedOrders) {
        if (changedOrders == null || changedOrders.isEmpty()) {
            return;
        }
        List<Long> orderIds = changedOrders.stream()
                .map(Order::getId)
                .toList();
        OrderPO update = new OrderPO();
        update.setStatus(OrderStatus.CANCELLED.getCode());
        update.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        LambdaUpdateWrapper<OrderPO> wrapper = new LambdaUpdateWrapper<OrderPO>()
                .in(OrderPO::getId, orderIds)
                .eq(OrderPO::getStatus, OrderStatus.PENDING.getCode());
        orderMapper.update(update, wrapper);
    }

    private Order toDomain(OrderPO po) {
        return Order.builder()
                .id(po.getId())
                .payType(po.getPayType() != null ? PayType.fromCode(po.getPayType()) : null)
                .updateTime(po.getUpdateTime())
                .to(po.getToName())
                .amount(po.getAmount())
                .status( OrderStatus.fromCode(po.getStatus()))
                .creatorId(po.getCreatorId())
                .sign(po.getSign())
                .packageId(po.getPackageId())
                .quantity(po.getQuantity())
                .couponId(po.getCouponId())
                .unitPrice(po.getUnitPrice())
                .discountAmount(po.getDiscountAmount())
                .createTime(po.getCreateTime())
                .timeoutTime(po.getTimeoutTime())
                .payTime(po.getPayTime())
                .build();
    }

    private OrderPO toPO(Order order) {
        return OrderPO.builder()
                .id(order.getId())
                .payType(order.getPayType() != null ? order.getPayType().getCode() : null)
                .updateTime(order.getUpdateTime())
                .toName(order.getTo())
                .amount(order.getAmount())
                .status(order.getStatus().getCode())
                .creatorId(order.getCreatorId())
                .sign(order.getSign())
                .packageId(order.getPackageId())
                .quantity(order.getQuantity())
                .couponId(order.getCouponId())
                .unitPrice(order.getUnitPrice())
                .discountAmount(order.getDiscountAmount())
                .createTime(order.getCreateTime())
                .timeoutTime(order.getTimeoutTime())
                .payTime(order.getPayTime())
                .build();
    }
}
