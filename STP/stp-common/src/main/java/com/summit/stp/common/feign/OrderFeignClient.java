package com.summit.stp.common.feign;

import com.summit.stp.shared.application.vo.OrderQueryVO;
import com.summit.stp.shared.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * Order 微服务 Feign 客户端 - 供其他微服务调用订单查询能力
 */
@FeignClient(name = "stp-order-service", contextId = "orderFeignClient")
public interface OrderFeignClient {

    /**
     * 根据用户 ID 和优惠券 ID 列表查询关联的订单信息
     */
    @GetMapping("/order/internal/byCouponIds")
    Result<Map<Long, OrderQueryVO>> findOrderByCouponIds(@RequestParam("userId") Long userId, @RequestParam("couponIds") List<Long> couponIds);
}
