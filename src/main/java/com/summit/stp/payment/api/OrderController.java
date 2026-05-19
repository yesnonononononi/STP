package com.summit.stp.payment.api;

import com.summit.stp.payment.application.vo.OrderQueryVO;
import com.summit.stp.payment.application.service.OrderAppService;
import com.summit.stp.shared.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单接口层 Controller
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/order")
public class OrderController {
    private final OrderAppService orderAppService;

    @PostMapping("/query/{orderId}")
    public Result<OrderQueryVO> queryOrder(@PathVariable Long orderId) {
        OrderQueryVO orderQueryVO = orderAppService.queryById(orderId);
        return Result.success(orderQueryVO);
    }

    @PostMapping("/delete/{orderId}")
    public Result<Void> deleteOrder(@PathVariable Long orderId) {
        orderAppService.deleteById(orderId);
        return Result.success();
    }

    @PostMapping("/query/history")
    public Result<List<OrderQueryVO>> queryHistoryOrders(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize) {
        List<OrderQueryVO> history = orderAppService.queryHistory(page, pageSize);
        return Result.success(history);
    }

    @PostMapping("/ack/{orderId}")
    public Result<Void> ackOrder(@PathVariable Long orderId) {
        orderAppService.ackOrder(orderId);
        return Result.success();
    }
}
