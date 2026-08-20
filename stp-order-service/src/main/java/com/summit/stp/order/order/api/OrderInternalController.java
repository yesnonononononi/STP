package com.summit.stp.order.order.api;

import com.summit.stp.order.api.vo.OrderQueryVO;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.order.order.application.service.OrderAppService;
import com.summit.stp.order.order.domain.exception.OrderNotFoundException;
import com.summit.stp.order.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 订单内部接口 - 供其他微服务通过 Feign 调用
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/order/internal")
public class OrderInternalController {

    private final OrderRepository orderRepository;
    private final OrderAppService orderAppService;

    /**
     * 根据用户 ID 和优惠券 ID 列表查询关联的订单信息
     */
    @GetMapping("/byCouponIds")
    public Result<Map<Long, OrderQueryVO>> findOrderByCouponIds(
            @RequestParam Long userId,
            @RequestParam List<Long> couponIds) {
        if(couponIds.isEmpty()){
            return Result.success(Map.of());
        }
        Map<Long, OrderQueryVO> result = orderRepository.findOrderByCouponIds(userId, couponIds);
        return Result.success(result);
    }

    @GetMapping("/query/{orderNo}")
    Result<OrderQueryVO> findById(@PathVariable Long orderNo){
        return Result.success(orderAppService.queryById(orderNo));
    }
}

