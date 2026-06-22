package com.summit.stp.order.infrastructure.persistence;

import cn.hutool.core.lang.generator.SnowflakeGenerator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.order.application.vo.OrderQueryVO;
import com.summit.stp.order.domain.model.Order;
import com.summit.stp.order.domain.model.OrderStatus;
import com.summit.stp.order.domain.repository.OrderRepository;
import com.summit.stp.order.infrastructure.persistence.mapper.OrderMapper;
import com.summit.stp.order.infrastructure.persistence.po.OrderPO;
import com.summit.stp.payment.domain.model.PayType;
import com.summit.stp.shared.ThreadContext.UserHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

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
        OrderPO po = OrderPO.builder()
                .id(order.getId())
                .amount(order.getAmount())
                .creatorId(order.getCreatorId()) // 设置当前下单的用户ID为创建者ID
                .payType(order.getPayType() != null ? order.getPayType().getCode() : null)
                .toName(order.getTo())
                .sign(order.getSign())
                .status(order.getStatus().getCode()) // 持久化状态枚举编码
                .packageId(order.getPackageId())
                .quantity(order.getQuantity())
                .couponId(order.getCouponId())
                .unitPrice(order.getUnitPrice())
                .discountAmount(order.getDiscountAmount())
                .createTime(order.getCreateTime())
                .updateTime(order.getUpdateTime())
                .payTime(order.getPayTime())
                .build();
    
        System.out.println("[DEBUG] 保存订单: id=" + order.getId() + ", status=" + order.getStatus().getCode());
        if (order.getStatus() == OrderStatus.PENDING) {
            orderMapper.insert(po);
        } else {
            orderMapper.updateById(po);
        }
    }

    @Override
    public List<Order> queryHistoryOrders(long page, long pageSize) {
        Long uid = UserHolder.getUser().getId();
        LambdaQueryWrapper<OrderPO> eq = new LambdaQueryWrapper<OrderPO>().eq(OrderPO::getCreatorId, uid);
        Page<OrderPO> orderPOPage = orderMapper.selectPage(new Page<>(page, pageSize), eq);
        List<OrderPO> records = orderPOPage.getRecords();
        if (records.isEmpty()) {
            return List.of();
        }

        return records.stream()
                .map(po -> Order.builder()
                                .id(po.getId())
                        .amount(po.getAmount())
                        .creatorId(po.getCreatorId())
                        .payType(po.getPayType() != null ? PayType.fromCode(po.getPayType()) : null)
                        .to(po.getToName())
                        .sign(po.getSign())
                        .status(OrderStatus.fromCode(po.getStatus()))
                        .packageId(po.getPackageId())
                        .quantity(po.getQuantity())
                        .couponId(po.getCouponId())
                        .unitPrice(po.getUnitPrice())
                        .discountAmount(po.getDiscountAmount())
                        .createTime(po.getCreateTime())
                        .updateTime(po.getUpdateTime())
                        .payTime(po.getPayTime())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public Order findOrderById(Long orderId) {
        OrderPO po = orderMapper.selectById(orderId);
        if (po == null) {
            return null;
        }

        //查询套餐
        return Order.builder()
                .id(po.getId()) // 订单ID
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
                .payTime(po.getPayTime())
                .build();
    }

    @Override
    public void deleteById(Long orderId) {
        orderMapper.deleteById(orderId);
    }

    @Override
    public void timeout(Long orderId) {
        OrderPO po = new OrderPO();
        po.setId(orderId);
        po.setStatus(OrderStatus.CANCELLED.getCode());
        
        LambdaQueryWrapper<OrderPO> updateWrapper = new LambdaQueryWrapper<>();
        updateWrapper.eq(OrderPO::getId, orderId).eq(OrderPO::getStatus, OrderStatus.PENDING.getCode());
        
        orderMapper.update(po, updateWrapper);
    }

    @Override
    public Map<Long, OrderQueryVO> findOrderByCouponIds(Long currentUserId, List<Long> ids) {
        LambdaQueryWrapper<OrderPO> wrapper = new LambdaQueryWrapper<OrderPO>().in(OrderPO::getCouponId, ids).eq(OrderPO::getCreatorId, currentUserId);
        List<OrderPO> orderPOS = orderMapper.selectList(wrapper);
        return orderPOS.stream().collect(Collectors.toMap(OrderPO::getCouponId,po->
            OrderQueryVO.builder()
                    .orderId(po.getId())
                    .memberId(po.getPackageId())
                    .build()
        ));

    }
}
