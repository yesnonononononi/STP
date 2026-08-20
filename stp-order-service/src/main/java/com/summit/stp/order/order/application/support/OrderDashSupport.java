package com.summit.stp.order.order.application.support;

import com.summit.stp.order.api.vo.stats.OrderStatsOverviewVO;
import com.summit.stp.order.api.vo.stats.OrderTrendsVO;
import com.summit.stp.order.order.domain.model.stats.OrderOverviewStat;
import com.summit.stp.order.order.domain.model.stats.OrderTrendStat;
import com.summit.stp.order.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * 订单模块 Dashboard 数据查询底层支撑组件
 * 严格遵循 DDD 架构规范：基于 OrderRepository 仓储接口查询，绝不上 Mapper
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderDashSupport {

    private final OrderRepository orderRepository;

    public OrderStatsOverviewVO getOrderOverviewStats() {
        log.info("【订单模块】[DashSupport] 基于 OrderRepository 查询订单与 GMV 真实统计数据");
        OrderOverviewStat stat = orderRepository.countOrderOverview();
        double totalGmv = stat != null && stat.getTotalGmv() != null ? stat.getTotalGmv() : 0.0;
        double todayGmv = stat != null && stat.getTodayGmv() != null ? stat.getTodayGmv() : 0.0;
        return OrderStatsOverviewVO.builder()
                .totalGmv(String.format("￥%.2f", totalGmv))
                .todayGmv(String.format("￥%.2f", todayGmv))
                .gmvGrowthRate(stat != null && stat.getGmvGrowthRate() != null ? stat.getGmvGrowthRate() : "0.0%")
                .build();
    }

    public OrderTrendsVO getOrderTrendsStats(String period) {
        log.info("【订单模块】[DashSupport] 基于 OrderRepository 查询订单与 GMV 走势, 周期: {}", period);
        int days = "30d".equalsIgnoreCase(period) ? 30 : 7;
        OrderTrendStat trend = orderRepository.countOrderTrends(days);
        return OrderTrendsVO.builder()
                .dates(trend != null && trend.getDates() != null ? trend.getDates() : Collections.emptyList())
                .gmvList(trend != null && trend.getGmvList() != null ? trend.getGmvList() : Collections.emptyList())
                .orderCountList(trend != null && trend.getOrderCountList() != null ? trend.getOrderCountList() : Collections.emptyList())
                .build();
    }
}
