package com.summit.stp.admin.api.controller;

import com.summit.stp.admin.api.dto.CreateCouponRequest;
import com.summit.stp.admin.application.command.CreateCouponCommand;
import com.summit.stp.admin.application.service.AdminCouponItemsService;
import com.summit.stp.admin.application.vo.AdminCouponVO;
import com.summit.stp.common.annotation.Admin;
import com.summit.stp.common.annotation.Login;
import com.summit.stp.common.application.api.result.PageResult;
import com.summit.stp.common.application.api.result.Result;

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
        return adminCouponItemsService.list(keyword, status, page, pageSize);
    }
    @PostMapping("/save")
    public Result<Void> save(@RequestBody CreateCouponRequest request){
        CreateCouponCommand command = CreateCouponCommand.builder()
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
    public Result<Void> ban(Long id){
        return adminCouponItemsService.toggleBan(id,true);
    }
    @PostMapping("/unban")
    public Result<Void> unban(Long id){
        return adminCouponItemsService.toggleBan(id,false);
    }
    @PostMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id){
        return adminCouponItemsService.delete(id);
    }

}
