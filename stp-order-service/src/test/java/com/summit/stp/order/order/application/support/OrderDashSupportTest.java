package com.summit.stp.order.order.application.support;

import com.summit.stp.common.application.domain.exception.ParameterException;
import com.summit.stp.order.api.vo.stats.OrderStatsOverviewVO;
import com.summit.stp.order.api.vo.stats.OrderTrendsVO;
import com.summit.stp.order.order.domain.model.stats.OrderOverviewStat;
import com.summit.stp.order.order.domain.model.stats.OrderTrendStat;
import com.summit.stp.order.order.domain.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * OrderDashSupport 纯单元测试，覆盖统计数据组装与 period 解析边界。
 */
@ExtendWith(MockitoExtension.class)
class OrderDashSupportTest {

    @Mock
    private OrderRepository orderRepository;

    private OrderDashSupport support;

    @BeforeEach
    void setUp() {
        support = new OrderDashSupport(orderRepository);
    }

    @Test
    @DisplayName("getOrderOverviewStats: 正常格式化 GMV")
    void overview_normal() {
        when(orderRepository.countOrderOverview()).thenReturn(OrderOverviewStat.builder()
                .totalGmv(1234.5)
                .todayGmv(100.0)
                .gmvGrowthRate("5.0%")
                .build());

        OrderStatsOverviewVO vo = support.getOrderOverviewStats();

        assertThat(vo.getTotalGmv()).isEqualTo("￥1234.50");
        assertThat(vo.getTodayGmv()).isEqualTo("￥100.00");
        assertThat(vo.getGmvGrowthRate()).isEqualTo("5.0%");
    }

    @Test
    @DisplayName("getOrderOverviewStats: stat 为 null -> 默认 0")
    void overview_nullStat() {
        when(orderRepository.countOrderOverview()).thenReturn(null);

        OrderStatsOverviewVO vo = support.getOrderOverviewStats();

        assertThat(vo.getTotalGmv()).isEqualTo("￥0.00");
        assertThat(vo.getTodayGmv()).isEqualTo("￥0.00");
        assertThat(vo.getGmvGrowthRate()).isEqualTo("0.0%");
    }

    @Test
    @DisplayName("getOrderTrendsStats: 正常映射")
    void trends_normal() {
        when(orderRepository.countOrderTrends(7)).thenReturn(OrderTrendStat.builder()
                .dates(List.of("2024-01-01"))
                .gmvList(List.of(10.0))
                .orderCountList(List.of(1))
                .build());

        OrderTrendsVO vo = support.getOrderTrendsStats("7d");

        assertThat(vo.getDates()).containsExactly("2024-01-01");
        assertThat(vo.getGmvList()).containsExactly(10.0);
        assertThat(vo.getOrderCountList()).containsExactly(1);
    }

    @Test
    @DisplayName("getOrderTrendsStats: trend 为 null -> 空列表")
    void trends_nullStat() {
        when(orderRepository.countOrderTrends(7)).thenReturn(null);

        OrderTrendsVO vo = support.getOrderTrendsStats(null);

        assertThat(vo.getDates()).isEmpty();
        assertThat(vo.getGmvList()).isEmpty();
        assertThat(vo.getOrderCountList()).isEmpty();
    }

    @Test
    @DisplayName("resolveDays: null/空串默认 7 天")
    void resolveDays_default() {
        when(orderRepository.countOrderTrends(7)).thenReturn(null);

        support.getOrderTrendsStats(null);
        support.getOrderTrendsStats("");
        support.getOrderTrendsStats("   ");

        verify(orderRepository, org.mockito.Mockito.times(3)).countOrderTrends(7);
    }

    @Test
    @DisplayName("resolveDays: 7d/30d/90d 映射")
    void resolveDays_supported() {
        when(orderRepository.countOrderTrends(org.mockito.ArgumentMatchers.anyInt())).thenReturn(null);

        support.getOrderTrendsStats("7d");
        support.getOrderTrendsStats("30d");
        support.getOrderTrendsStats("90d");

        verify(orderRepository).countOrderTrends(7);
        verify(orderRepository).countOrderTrends(30);
        verify(orderRepository).countOrderTrends(90);
    }

    @Test
    @DisplayName("resolveDays: 非法周期 -> ParameterException")
    void resolveDays_invalid() {
        assertThatThrownBy(() -> support.getOrderTrendsStats("1y"))
                .isInstanceOf(ParameterException.class);
    }
}
