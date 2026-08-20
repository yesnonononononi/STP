package com.summit.stp.order.order.api;

import com.summit.stp.common.annotation.Login;
import com.summit.stp.order.api.vo.OrderQueryVO;
import com.summit.stp.payment.api.vo.PayVO;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.order.order.api.dto.OrderCreateRequest;
import com.summit.stp.order.order.application.service.OrderAppService;
import com.summit.stp.order.order.application.vo.OrderCreateVO;
import com.summit.stp.order.order.application.service.OrderReconciliationAppService;
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
@Login
@RestController
@RequiredArgsConstructor
@RequestMapping("/order")
public class OrderController {
    private final OrderAppService orderAppService;

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
            @ApiParam(value = "每页数量", defaultValue = "10") @RequestParam(defaultValue = "10") long pageSize,
            @ApiParam(value = "订单状态", required = false) @RequestParam(required = false) Integer status) {
        List<OrderQueryVO> history = orderAppService.queryHistory(page, pageSize, status);
        return Result.success(history);
    }

    @GetMapping("/ack")
    public Result<Void> ack(Long orderNo){
        return orderAppService.ack(orderNo);
    }


    @ApiOperation(value = "创建新订单并返回支付参数")
    @PostMapping("/create")
    public Result<OrderCreateVO> createOrder(
            @ApiParam(value = "订单创建请求参数", required = true) @RequestBody OrderCreateRequest orderCreateRequest){
        return orderAppService.createOrder(orderCreateRequest);
    }
}

