package com.summit.stp.order.order.api;

import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.order.api.vo.stats.OrderStatsOverviewVO;
import com.summit.stp.order.api.vo.stats.OrderTrendsVO;
import com.summit.stp.order.order.application.support.OrderDashSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order/internal/stats")
@RequiredArgsConstructor
public class OrderInternalStatsController {

    private final OrderDashSupport orderDashSupport;

    @GetMapping("/overview")
    public Result<OrderStatsOverviewVO> getOrderOverviewStats() {
        return Result.success(orderDashSupport.getOrderOverviewStats());
    }

    @GetMapping("/trends")
    public Result<OrderTrendsVO> getOrderTrendsStats(@RequestParam("period") String period) {
        return Result.success(orderDashSupport.getOrderTrendsStats(period));
    }
}
