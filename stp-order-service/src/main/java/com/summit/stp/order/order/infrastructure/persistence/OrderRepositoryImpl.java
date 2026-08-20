package com.summit.stp.order.order.infrastructure.persistence;

import cn.hutool.core.lang.generator.SnowflakeGenerator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.common.application.domain.model.PayType;
import com.summit.stp.common.auth.UserHolder;
import com.summit.devframeworkdddstarter.repo.AbstractRepository;
import com.summit.stp.order.admin.application.command.AdminQueryCommand;
import com.summit.stp.order.api.vo.OrderQueryVO;
import com.summit.stp.order.order.domain.model.Order;
import com.summit.stp.order.order.domain.model.OrderStatus;
import com.summit.stp.order.order.domain.repository.OrderRepository;
import com.summit.stp.order.order.infrastructure.persistence.dto.OrderOverviewStatDTO;
import com.summit.stp.order.order.infrastructure.persistence.dto.OrderTrendStatDTO;
import com.summit.stp.order.order.infrastructure.persistence.mapper.OrderMapper;
import com.summit.stp.order.order.infrastructure.persistence.po.OrderPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.summit.stp.common.util.TrendDateUtil;
import com.summit.stp.order.order.domain.model.stats.OrderOverviewStat;
import com.summit.stp.order.order.domain.model.stats.OrderTrendStat;
import com.summit.stp.order.api.vo.stats.OrderStatsOverviewVO;
import com.summit.stp.order.api.vo.stats.OrderTrendsVO;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class OrderRepositoryImpl extends AbstractRepository<Order, OrderPO> implements OrderRepository {
    @Autowired
    private  OrderMapper orderMapper;

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
        super.save(order);
    }

    @Override
    public void updateById(Order order) {
        if (order == null) return;
        super.updateById(order);
    }

    @Override
    public List<Order> queryHistoryOrders(long page, long pageSize, Integer status) {
        Long uid = UserHolder.getUser().getId();
        LambdaQueryWrapper<OrderPO> wrapper = new LambdaQueryWrapper<OrderPO>()
                .eq(OrderPO::getCreatorId, uid)
                .eq(status != null, OrderPO::getStatus, status)
                .orderByDesc(OrderPO::getId);
        Page<OrderPO> orderPOPage = getBaseMapper().selectPage(new Page<>(page, pageSize), wrapper);
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
    public Page<Order> findPage(AdminQueryCommand command) {
        Integer payStatus = command.getPayStatus();
        String orderNo = command.getOrderNo();
        Integer orderType = command.getOrderType();
        Long userId = command.getUserId();
        Page<Order> res = new Page<>();
        Page<OrderPO> p = new Page<>(Objects.requireNonNullElse(command.getPage(), 1), Objects.requireNonNullElse(command.getSize(), 10));
        LambdaQueryWrapper<OrderPO> wrapper = new LambdaQueryWrapper<>();
        if (payStatus != null) wrapper.eq(OrderPO::getStatus, payStatus);
        if (orderNo != null) wrapper.eq(OrderPO::getId, orderNo);
        if (orderType != null) wrapper.eq(OrderPO::getPayType, orderType);
        if (userId != null) wrapper.eq(OrderPO::getCreatorId, userId);
        p = getBaseMapper().selectPage(p, wrapper);
        return res.setCurrent(p.getCurrent()).setTotal(p.getTotal()).setRecords(p.getRecords().stream().map(this::toModel).toList());

    }

    @Override
    public void ackOrderIfWaitPay(Long orderNo) {
        orderMapper.ackOrderIfWaitPay(orderNo,OrderStatus.PAID.getCode(), OrderStatus.PENDING.getCode());
    }

    @Override
    public OrderOverviewStat countOrderOverview() {
        LocalDateTime todayMin = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        Timestamp startOfToday = Timestamp.valueOf(todayMin);
        Timestamp startOfYesterday = Timestamp.valueOf(todayMin.minusDays(1));
        OrderOverviewStatDTO dto = orderMapper.selectOrderOverviewStats(startOfToday, startOfYesterday);

        double totalGmvVal = (dto != null && dto.getTotalGmv() != null) ? dto.getTotalGmv() : 0.0;
        double todayGmvVal = (dto != null && dto.getTodayGmv() != null) ? dto.getTodayGmv() : 0.0;
        double yesterdayGmvVal = (dto != null && dto.getYesterdayGmv() != null) ? dto.getYesterdayGmv() : 0.0;

        String gmvGrowthRate = (yesterdayGmvVal > 0)
                ? String.format("%.1f%%", (todayGmvVal - yesterdayGmvVal) * 100.0 / yesterdayGmvVal)
                : (todayGmvVal > 0 ? "+100.0%" : "0.0%");

        return OrderOverviewStat.builder()
                .totalGmv(totalGmvVal)
                .todayGmv(todayGmvVal)
                .gmvGrowthRate(gmvGrowthRate)
                .build();
    }

    @Override
    public OrderTrendStat countOrderTrends(int days) {
        int limitDays = (days > 0 && days <= 60) ? days : 7;
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(limitDays - 1);
        Timestamp startTimestamp = Timestamp.valueOf(startDate.atStartOfDay());

        List<OrderTrendStatDTO> dtoList = orderMapper.selectOrderTrendsStats(startTimestamp);
        Map<String, OrderTrendStatDTO> resultMap = (dtoList != null) ? dtoList.stream()
                .filter(d -> d.getDateStr() != null)
                .collect(Collectors.toMap(OrderTrendStatDTO::getDateStr, d -> d, (k1, k2) -> k1)) : Map.of();

        var gmvTrend = TrendDateUtil.buildTrendData(
                limitDays, resultMap, d -> d.getDayGmv() != null ? Math.round(d.getDayGmv() * 100.0) / 100.0 : 0.0, 0.0
        );

        var countTrend = TrendDateUtil.buildTrendData(
                limitDays, resultMap, d -> d.getDayCount() != null ? d.getDayCount() : 0, 0
        );

        return OrderTrendStat.builder()
                .dates(gmvTrend.dates())
                .gmvList(gmvTrend.values())
                .orderCountList(countTrend.values())
                .build();
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


