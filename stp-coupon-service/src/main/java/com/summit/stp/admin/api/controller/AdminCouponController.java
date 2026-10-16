package com.summit.stp.admin.api.controller;

import com.summit.stp.admin.api.dto.CreateCouponRequest;
import com.summit.stp.admin.application.command.CreateCouponCommand;
import com.summit.stp.admin.application.service.AdminCouponItemsService;
import com.summit.stp.admin.application.vo.AdminCouponVO;
import com.summit.stp.common.annotation.Admin;
import com.summit.stp.common.annotation.Login;
import com.summit.stp.common.application.api.result.PageResult;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.operationlog.annotation.OperationLog;
import com.summit.stp.operationlog.domain.model.Operation;

import com.summit.stp.coupon.domain.model.Coupon;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Admin
@Login
@RestController
@RequestMapping("/a/coupon")
@RequiredArgsConstructor
public class AdminCouponController {
    private final AdminCouponItemsService adminCouponItemsService;


    @GetMapping("/list")
    public Result<PageResult<List<AdminCouponVO>>> list(String keyword, Integer status, Integer page, Integer pageSize){
        return adminCouponItemsService.list(keyword, status, clampPage(page), clampSize(pageSize));
    }
    @PostMapping("/save")
    @OperationLog(description = "保存优惠券", type = Operation.OperationType.CREATE)
    public Result<Void> save(@RequestBody CreateCouponRequest request){
        CreateCouponCommand command = CreateCouponCommand.builder()
                .id(request.getId())
                .name(request.getName())
                .discount(request.getDiscount())
                .amount(request.getAmount())
                .status(request.getStatus())
                .type(request.getType())
                .description(request.getDescription())
                .image(request.getImage())
                .scopeType(Coupon.CouponScopeType.fromCode(request.getScopeType()))
                .scopeRelationIds(request.getScopeRelationIds())
                .timeType(Coupon.CouponDateType.getByCode(request.getTimeType()))
                .validDays(request.getValidDays())
                .validHours(request.getValidHours())
                .build();
        return adminCouponItemsService.create(command);
    }
    @PostMapping("/ban")
    @OperationLog(description = "禁用优惠券", type = Operation.OperationType.UPDATE, entityId = "#id")
    public Result<Void> ban(Long id){
        return adminCouponItemsService.toggleBan(id,true);
    }
    @PostMapping("/unban")
    @OperationLog(description = "启用优惠券", type = Operation.OperationType.UPDATE, entityId = "#id")
    public Result<Void> unban(Long id){
        return adminCouponItemsService.toggleBan(id,false);
    }
    @PostMapping("/delete/{id}")
    @OperationLog(description = "删除优惠券", type = Operation.OperationType.DELETE, entityId = "#id")
    public Result<Void> delete(@PathVariable Long id){
        return adminCouponItemsService.delete(id);
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
