package com.summit.stp.coupon.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.coupon.application.service.CouponAppService;
import com.summit.stp.coupon.application.vo.CouponQueryVO;
import com.summit.stp.shared.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/coupon")
@RestController
@RequiredArgsConstructor
public class CouponController {
    private final CouponAppService couponAppService;

    /**
     * 获取当前用户未使用的可用优惠券
     */
    @GetMapping("/list")
    public Result<List<CouponQueryVO>> getAvailableCoupons() {
        return Result.success(couponAppService.queryAvailableCoupons());
    }

    /**
     * 分页获取当前用户的优惠券历史（含已使用、已过期等）
     */
    @GetMapping("/history")
    public Result<Page<CouponQueryVO>> getCouponHistory(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.success(couponAppService.queryHistory(page, pageSize));
    }
}
