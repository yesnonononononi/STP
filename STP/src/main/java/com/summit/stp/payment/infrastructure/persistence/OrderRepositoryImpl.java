package com.summit.stp.payment.infrastructure.persistence;

import cn.hutool.core.lang.generator.SnowflakeGenerator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.payment.domain.exception.OrderDuplicationException;
import com.summit.stp.payment.domain.model.Order;
import com.summit.stp.payment.domain.repository.OrderRepository;
import com.summit.stp.payment.infrastructure.Enum.PayType;
import com.summit.stp.payment.infrastructure.persistence.mapper.OrderMapper;
import com.summit.stp.payment.infrastructure.persistence.po.OrderPO;
import com.summit.stp.shared.ThreadContext.UserHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

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
        OrderPO existing = orderMapper.selectById(order.getId());
        OrderPO po = OrderPO.builder()
                .id(order.getId())
                .price(order.getAmount())
                .creatorId(UserHolder.getUser().getId()) // 设置当前下单的用户ID为创建者ID
                .payType(order.getPayType() != null ? order.getPayType().getCode() : null)
                .toName(order.getTo())
                .sign(order.getSign())
                .status(order.getStatus().getCode()) // 持久化状态枚举编码
                .createTime(order.getCreateTime())
                .updateTime(order.getUpdateTime())
                .build();
                
        if (existing == null) {
            try {
                orderMapper.insert(po);
            } catch (DuplicateKeyException e) {
                throw new OrderDuplicationException(String.format("订单号%s已存在", order.getId()));
            }
        } else {
            orderMapper.updateById(po); // 存在则执行更新，使状态变更能正确存回数据库
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
                .map(po -> Order.reconstitute(
                        po.getId(),
                        po.getPrice(),
                        po.getPayType() != null ? PayType.fromCode(po.getPayType()) : null,
                        po.getToName(),
                        po.getSign(),
                        po.getStatus(),
                        po.getCreateTime(),
                        po.getUpdateTime()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public Order findOrderById(Long orderId) {
        OrderPO po = orderMapper.selectById(orderId);
        if (po == null) {
            return null;
        }
        return Order.reconstitute(
                po.getId(),
                po.getPrice(),
                po.getPayType() != null ? PayType.fromCode(po.getPayType()) : null,
                po.getToName(),
                po.getSign(),
                po.getStatus(),
                po.getCreateTime(),
                po.getUpdateTime()
        );
    }

    @Override
    public void deleteById(Long orderId) {
        orderMapper.deleteById(orderId);
    }
}
