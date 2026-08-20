package com.summit.stp.admin.api.controller;

import com.summit.stp.admin.api.dto.CreateCouponActivityRequest;
import com.summit.stp.admin.application.command.CreateCouponActivityCommand;
import com.summit.stp.admin.application.service.AdminCouponItemsService;
import com.summit.stp.admin.application.vo.AdminCouponActivityVO;
import com.summit.stp.common.application.api.result.PageResult;
import com.summit.stp.common.application.api.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/a/coupon/activity")
public class AdminCouponActivityController {
    private final AdminCouponItemsService adminCouponActivityService;

    @GetMapping("/list")
    public Result<PageResult<List<AdminCouponActivityVO>>> list(String keyword, Integer status, Integer page, Integer pageSize){
        return adminCouponActivityService.listActivity(keyword, status, page, pageSize);
    }
    @PostMapping("/save")
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
    public Result<Void> start(Long id){
        adminCouponActivityService.toggleActivityStatus(id,true);
        return Result.success();
    }
    @PostMapping("/close")
    public Result<Void> close(Long id){
        adminCouponActivityService.toggleActivityStatus(id,false);
        return Result.success();
    }
    @PostMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id){
        adminCouponActivityService.deleteActivity(id);
        return Result.success();
    }
}
