package com.summit.stp.admin.api.controller;

import com.summit.stp.admin.api.dto.CreateCouponActivityRequest;
import com.summit.stp.admin.application.command.CreateCouponActivityCommand;
import com.summit.stp.admin.application.service.AdminCouponItemsService;
import com.summit.stp.admin.application.vo.AdminCouponActivityVO;
import com.summit.stp.common.annotation.Admin;
import com.summit.stp.common.annotation.Login;
import com.summit.stp.common.application.api.result.PageResult;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.operationlog.annotation.OperationLog;
import com.summit.stp.operationlog.domain.model.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Admin
@Login
@RestController
@RequiredArgsConstructor
@RequestMapping("/a/coupon/activity")
public class AdminCouponActivityController {
    private final AdminCouponItemsService adminCouponActivityService;

    @GetMapping("/list")
    public Result<PageResult<List<AdminCouponActivityVO>>> list(String keyword, Integer status, Integer page, Integer pageSize){
        return adminCouponActivityService.listActivity(keyword, status, clampPage(page), clampSize(pageSize));
    }
    @PostMapping("/save")
    @OperationLog(description = "保存优惠券活动", type = Operation.OperationType.CREATE)
    public Result<Void> save(@RequestBody CreateCouponActivityRequest request){
        CreateCouponActivityCommand command = CreateCouponActivityCommand.builder()
                .id(request.getId())
                .couponId(request.getCouponId())
                .name(request.getName())
                .stock(request.getStock())
                .activityStartTime(request.getActivityStartTime())
                .activityEndTime(request.getActivityEndTime())
                .status(request.getStatus())
                .type(request.getType())
                .build();
        adminCouponActivityService.createActivity(command);
        return Result.success();
    }
    @PostMapping("/start")
    @OperationLog(description = "开启优惠券活动", type = Operation.OperationType.UPDATE, entityId = "#id")
    public Result<Void> start(Long id){
        adminCouponActivityService.toggleActivityStatus(id,true);
        return Result.success();
    }
    @PostMapping("/close")
    @OperationLog(description = "关闭优惠券活动", type = Operation.OperationType.UPDATE, entityId = "#id")
    public Result<Void> close(Long id){
        adminCouponActivityService.toggleActivityStatus(id,false);
        return Result.success();
    }
    @PostMapping("/delete/{id}")
    @OperationLog(description = "删除优惠券活动", type = Operation.OperationType.DELETE, entityId = "#id")
    public Result<Void> delete(@PathVariable Long id){
        adminCouponActivityService.deleteActivity(id);
        return Result.success();
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
