package com.summit.stp.order.infrastructure.persistence;

import cn.hutool.core.lang.generator.SnowflakeGenerator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.common.application.domain.model.PayType;
import com.summit.stp.common.auth.UserHolder;
import com.summit.devframeworkdddstarter.repo.AbstractRepository;
import com.summit.stp.order.api.vo.OrderQueryVO;
import com.summit.stp.order.domain.model.Order;
import com.summit.stp.order.domain.model.OrderStatus;
import com.summit.stp.order.domain.repository.OrderRepository;
import com.summit.stp.order.infrastructure.persistence.po.OrderPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class OrderRepositoryImpl extends AbstractRepository<Order, OrderPO> implements OrderRepository {

    public OrderRepositoryImpl(BaseMapper<OrderPO> baseMapper) {
        super(baseMapper);
    }

    @Override
    public Long generateOrderId() {
        return new SnowflakeGenerator().next();
    }

    @Override
    public void save(Order order) {
        if (order == null) return;
        if (order.getId() != null && findById(order.getId()).isPresent()) {
            super.updateById(order);
        } else {
            super.save(order);
        }
    }

    @Override
    public List<Order> queryHistoryOrders(long page, long pageSize) {
        Long uid = UserHolder.getUser().getId();
        LambdaQueryWrapper<OrderPO> eq = new LambdaQueryWrapper<OrderPO>()
                .eq(OrderPO::getCreatorId, uid)
                .orderByDesc(OrderPO::getId);
        Page<OrderPO> orderPOPage = getBaseMapper().selectPage(new Page<>(page, pageSize), eq);
        List<OrderPO> records = orderPOPage.getRecords();
        if (records.isEmpty()) {
            return List.of();
        }
        return records.stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public Order findOrderById(Long orderId) {
        return findBy(orderId, OrderPO::getId).orElse(null);
    }

    @Override
    public void deleteById(Long orderId) {
        delete(orderId, OrderPO::getId);
    }

    @Override
    public Map<Long, OrderQueryVO> findOrderByCouponIds(Long currentUserId, List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Map.of();
        LambdaQueryWrapper<OrderPO> wrapper = new LambdaQueryWrapper<OrderPO>()
                .in(OrderPO::getCouponId, ids)
                .eq(OrderPO::getCreatorId, currentUserId)
                .in(OrderPO::getStatus, OrderStatus.PAID.getCode(), OrderStatus.COMPLETED.getCode());
        List<OrderPO> orderPOS = getBaseMapper().selectList(wrapper);
        return orderPOS.stream().collect(Collectors.toMap(OrderPO::getCouponId, po ->
                OrderQueryVO.builder()
                        .orderId(po.getId())
                        .memberId(po.getPackageId())
                        .build()
        ));
    }

    @Override
    public List<Order> findOrderByIds(Collection<Long> orderIds) {
        return findListIn(orderIds, OrderPO::getId);
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
        return getBaseMapper().selectPage(page, wrapper).getRecords().stream()
                .map(this::toModel)
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
        getBaseMapper().update(update, wrapper);
    }

    @Override
    protected Order toModel(OrderPO po) {
        if (po == null) return null;
        return Order.builder()
                .id(po.getId())
                .payType(po.getPayType() != null ? PayType.fromCode(po.getPayType()) : null)
                .updateTime(po.getUpdateTime())
                .to(po.getToName())
                .amount(po.getAmount())
                .status(OrderStatus.fromCode(po.getStatus()))
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

    @Override
    protected OrderPO toPO(Order order) {
        if (order == null) return null;
        return OrderPO.builder()
                .id(order.getId())
                .payType(order.getPayType() != null ? order.getPayType().getCode() : null)
                .updateTime(order.getUpdateTime())
                .toName(order.getTo())
                .amount(order.getAmount())
                .status(order.getStatus() != null ? order.getStatus().getCode() : null)
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


