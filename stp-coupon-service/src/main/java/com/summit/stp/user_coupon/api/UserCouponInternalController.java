package com.summit.stp.user_coupon.api;

import com.summit.stp.coupon.api.vo.CouponQueryVO;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.user_coupon.application.service.UserCouponAppService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/coupon/internal")
@RequiredArgsConstructor
@Api(tags = "优惠券内部服务接口")
public class UserCouponInternalController {
    private final UserCouponAppService userCouponAppService;

    @GetMapping("/get/{id}")
    @ApiOperation(value = "根据ID查询用户优惠券详情")
    public Result<CouponQueryVO> queryById(@PathVariable("id") Long id) {
        return Result.success(userCouponAppService.queryById(id));
    }

    @PostMapping("/use")
    @ApiOperation(value = "订单核销优惠券")
    public Result<Void> use(@RequestParam("id") Long id,
                            @RequestParam("orderId") Long orderId,
                            @RequestParam("typeId") Long typeId,
                            @RequestParam("packageId") Long packageId) {
        userCouponAppService.use(id, orderId, typeId, packageId);
        return Result.success();
    }

    @PostMapping("/refund")
    @ApiOperation(value = "订单退还优惠券")
    public Result<Void> refund(@RequestParam("id") Long id) {
        userCouponAppService.refund(id);
        return Result.success();
    }

    @GetMapping("/calculate")
    @ApiOperation(value = "计算订单优惠后金额")
    public Result<BigDecimal> calculateAmount(
            @RequestParam("price") BigDecimal price,
            @RequestParam("quantity") Integer quantity,
            @RequestParam(value = "couponId", required = false) Long couponId) {
        return Result.success(userCouponAppService.calculateAmount(price, quantity, couponId));
    }

    @PostMapping("/batch")
    @ApiOperation(value = "批量查询优惠券信息")
    public Result<Map<Long, CouponQueryVO>> queryByIds(@RequestBody List<Long> cList) {
        return Result.success(userCouponAppService.queryByIds(cList));
    }
}

