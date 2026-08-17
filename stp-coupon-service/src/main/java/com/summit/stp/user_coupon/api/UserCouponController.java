package com.summit.stp.user_coupon.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.common.annotation.Login;
import com.summit.stp.coupon.api.vo.CouponQueryVO;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.user_coupon.application.service.UserCouponAppService;
import com.summit.stp.user_coupon.domain.model.CouponStatus;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Login
@RequestMapping("/coupon")
@RestController
@RequiredArgsConstructor
@Api(tags = "用户优惠券资产管理")
public class UserCouponController {
    private final UserCouponAppService userCouponAppService;

    @GetMapping("/list")
    @ApiOperation(value = "获取当前用户可用的未使用优惠券")
    public Result<List<CouponQueryVO>> getAvailableCoupons() {
        return Result.success(userCouponAppService.queryAvailableCoupons());
    }

    @GetMapping("/history")
    @ApiOperation(value = "分页获取当前用户的优惠券历史")
    public Result<Page<CouponQueryVO>> getCouponHistory(
            @ApiParam("页码") @RequestParam(defaultValue = "1") long page,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") long pageSize,
            @ApiParam("优惠券状态") @RequestParam(required = false) Integer status
    ) {
        return Result.success(userCouponAppService.queryHistory(page, pageSize, status == null ? null : CouponStatus.fromCode(status)));
    }

    @GetMapping("/order-list")
    @ApiOperation(value = "根据商品分类ID和具体套餐ID获取用户所有优惠券并标识本单是否可用")
    public Result<List<CouponQueryVO>> getCouponsForOrder(
            @ApiParam("商品分类ID") @RequestParam("typeId") Long typeId,
            @ApiParam("具体套餐ID") @RequestParam("packageId") Long packageId) {
        return Result.success(userCouponAppService.queryCouponsForOrder(typeId, packageId));
    }
}

