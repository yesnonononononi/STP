package com.summit.stp.order.api;

import com.summit.stp.order.api.dto.OrderCreateRequest;
import com.summit.stp.order.application.service.OrderAppService;
import com.summit.stp.order.application.service.OrderReconciliationAppService;
import com.summit.stp.order.application.vo.OrderQueryVO;
import com.summit.stp.payment.application.vo.PayVO;
import com.summit.stp.shared.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单接口层 Controller
 */
@Api(tags = "订单管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/order")
public class OrderController {
    private final OrderAppService orderAppService;
    private final OrderReconciliationAppService orderReconciliationAppService;

    @ApiOperation(value = "根据订单ID查询订单详情")
    @PostMapping("/query/{orderId}")
    public Result<OrderQueryVO> queryOrder(
            @ApiParam(value = "订单ID", required = true) @PathVariable Long orderId) {
        OrderQueryVO orderQueryVO = orderAppService.queryById(orderId);

        return Result.success(orderQueryVO);
    }

    @ApiOperation(value = "根据订单ID删除订单")
    @PostMapping("/delete/{orderId}")
    public Result<Void> deleteOrder(
            @ApiParam(value = "订单ID", required = true) @PathVariable Long orderId) {
        orderAppService.deleteById(orderId);
        return Result.success();
    }

    @ApiOperation(value = "分页查询历史订单列表")
    @PostMapping("/query/history")
    public Result<List<OrderQueryVO>> queryHistoryOrders(
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") long page,
            @ApiParam(value = "每页数量", defaultValue = "10") @RequestParam(defaultValue = "10") long pageSize) {
        List<OrderQueryVO> history = orderAppService.queryHistory(page, pageSize);
        return Result.success(history);
    }

    @ApiOperation(value = "确认订单（接单/完成等）")
    @PostMapping("/ack/{orderId}")
    public Result<Void> ackOrder(
            @ApiParam(value = "订单ID", required = true) @PathVariable Long orderId) {
        orderAppService.ackOrder(orderId);
        return Result.success();
    }

    @ApiOperation(value = "手动触发订单对账逻辑")
    @PostMapping("/reconcile/{orderId}")
    public Result<String> reconcileOrder(
            @ApiParam(value = "订单ID", required = true) @PathVariable Long orderId) {
        return orderReconciliationAppService.reconcileOrder(orderId);
    }

    @ApiOperation(value = "创建新订单并返回支付参数")
    @PostMapping("/create")
    public Result<PayVO> createOrder(
            @ApiParam(value = "订单创建请求参数", required = true) @RequestBody OrderCreateRequest orderCreateRequest){
        return orderAppService.createOrder(orderCreateRequest);
    }
}
