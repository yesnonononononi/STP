package com.summit.stp.order.api.client;

import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.order.api.vo.OrderQueryVO;
import com.summit.stp.order.api.vo.stats.OrderStatsOverviewVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * Order 微服务 Feign 客户端
 */
@FeignClient(name = "stp-order-service", contextId = "orderFeignClient")
public interface OrderFeignClient {

    @GetMapping("/order/internal/byCouponIds")
    Result<Map<Long, OrderQueryVO>> findOrderByCouponIds(@RequestParam("userId") Long userId, @RequestParam("couponIds") List<Long> couponIds);

    @GetMapping("/order/internal/query/{orderNo}")
    Result<OrderQueryVO> findById(@PathVariable Long orderNo);

    @GetMapping("/order/internal/stats/overview")
    Result<OrderStatsOverviewVO> getOrderOverviewStats();

    @GetMapping("/order/internal/stats/trends")
    Result<com.summit.stp.order.api.vo.stats.OrderTrendsVO> getOrderTrendsStats(@RequestParam("period") String period);
}
