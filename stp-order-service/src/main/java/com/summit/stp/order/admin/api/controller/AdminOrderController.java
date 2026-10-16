package com.summit.stp.order.admin.api.controller;

import com.summit.stp.common.annotation.Admin;
import com.summit.stp.common.annotation.Login;
import com.summit.stp.common.application.api.result.PageResult;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.operationlog.annotation.OperationLog;
import com.summit.stp.operationlog.domain.model.Operation;
import com.summit.stp.order.admin.api.dto.AdminOrderQueryRequest;
import com.summit.stp.order.admin.application.command.AdminQueryCommand;
import com.summit.stp.order.admin.application.service.AdminOrderService;
import com.summit.stp.order.admin.application.vo.AdminOrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@Admin
@Login
@RestController
@RequiredArgsConstructor
@RequestMapping("a/order")
public class AdminOrderController {
    private final AdminOrderService adminOrderService;

    @PostMapping("/list")
    public Result<PageResult<List<AdminOrderVO>>> listBy(AdminOrderQueryRequest request){
        AdminQueryCommand command = AdminQueryCommand.builder()
                .page(clampPage(request.getPage()))
                .size(clampSize(request.getSize()))
                .orderNo(request.getOrderNo())
                .userId(request.getUserId())
                .orderType(request.getOrderType())
                .payStatus(request.getPayStatus())
                .build();
        return adminOrderService.listBy(command);
    }
    @PostMapping("/reissuance")
    @OperationLog(description = "订单补发", type = Operation.OperationType.UPDATE, entityId = "#orderNo")
    public Result<Void> reissuance(Long orderNo){
        return adminOrderService.reissuance(orderNo);
    }
    @PostMapping("/refund")
    @OperationLog(description = "订单退款", type = Operation.OperationType.UPDATE, entityId = "#orderNo")
    public Result<Void> refund(Long orderNo,String reason){
        return adminOrderService.refund(orderNo,reason);
    }
    @PostMapping("/verfiy-status")
    public Result<String> verifyStatus(Long orderNo){
        return adminOrderService.verifyStatus(orderNo);
    }

    private static Integer clampPage(Integer page) {
        return (page == null || page < 1) ? 1 : page;
    }

    private static Integer clampSize(Integer size) {
        if (size == null || size < 1) {
            return 10;
        }
        return Math.min(size, 100);
    }
}
