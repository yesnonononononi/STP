package com.summit.stp.coupon.api;

import com.summit.stp.common.application.api.vo.CouponQueryVO;
import com.summit.stp.common.result.Result;
import com.summit.stp.coupon.application.service.CouponAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/coupon/internal")
@RequiredArgsConstructor
public class CouponInternalController {

    private final CouponAppService couponAppService;

    @GetMapping("/get/{id}")
    public Result<CouponQueryVO> queryById(@PathVariable("id") Long id) {
        return Result.success(couponAppService.queryById(id));
    }

    @PostMapping("/use")
    public Result<Void> use(@RequestParam("id") Long id, @RequestParam("orderId") Long orderId,
                            @RequestParam("typeId") Long typeId, @RequestParam("packageId") Long packageId) {
        couponAppService.use(id, orderId,typeId,packageId);
        return Result.success();
    }

    @PostMapping("/refund")
    public Result<Void> refund(@RequestParam("id") Long id) {
        couponAppService.refund(id);
        return Result.success();
    }

    @GetMapping("/calculate")
    public Result<BigDecimal> calculateAmount(
            @RequestParam("price") BigDecimal price,
            @RequestParam("quantity") Integer quantity,
            @RequestParam(value = "couponId", required = false) Long couponId) {
        return Result.success(couponAppService.calculateAmount(price, quantity, couponId));
    }


    @PostMapping("/batch")
    public Result<Map<Long, CouponQueryVO>> queryByIds(@RequestBody List<Long> cList) {
        return Result.success(couponAppService.queryByIds(cList));
    }
}
