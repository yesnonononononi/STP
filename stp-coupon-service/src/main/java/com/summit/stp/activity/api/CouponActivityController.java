package com.summit.stp.activity.api;

import com.summit.stp.activity.application.dto.CouponActivityDTO;
import com.summit.stp.activity.application.service.CouponActivityAppService;
import com.summit.stp.activity.application.vo.CouponActivityQueryVO;
import com.summit.stp.common.annotation.Login;
import com.summit.stp.common.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Login
@RequestMapping("/coupon/activity")
@RestController
@RequiredArgsConstructor
@Api(tags = "优惠券活动管理")
public class CouponActivityController {
    private final CouponActivityAppService couponActivityAppService;

    @GetMapping("/list/{type}")
    @ApiOperation(value = "获取所有可供领取的优惠券投放活动列表")
    public Result<List<CouponActivityQueryVO>> getCouponActivitiesByScopeType(
            @ApiParam("范围类型") @PathVariable Integer type) {
        return Result.success(couponActivityAppService.queryAllActivitiesByScopeType(type));
    }

    @GetMapping("/receive/{activityId}")
    @ApiOperation(value = "领取活动优惠券")
    public Result<Void> receiveCoupon(
            @ApiParam("活动ID") @PathVariable Long activityId) {
        couponActivityAppService.receiveActivityCoupon(activityId);
        return Result.success();
    }

    @PostMapping("")
    @ApiOperation(value = "新增优惠券投放活动")
    public Result<Void> addActivity(@RequestBody CouponActivityDTO dto) {
        couponActivityAppService.saveActivity(dto);
        return Result.success();
    }

    @PutMapping("")
    @ApiOperation(value = "更新优惠券投放活动")
    public Result<Void> updateActivity(@RequestBody CouponActivityDTO dto) {
        couponActivityAppService.updateActivity(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除指定的优惠券投放活动")
    public Result<Void> deleteActivity(
            @ApiParam("活动ID") @PathVariable("id") Long id) {
        couponActivityAppService.deleteActivity(id);
        return Result.success();
    }
}
