package com.summit.stp.coupon.api;

import com.summit.stp.common.annotation.Login;
import com.summit.stp.coupon.api.vo.CouponQueryVO;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.coupon.application.service.CouponAppService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Login
@RequestMapping("/coupon/template")
@RestController
@RequiredArgsConstructor
@Api(tags = "优惠券模板定义管理")
public class CouponTemplateController {
    private final CouponAppService couponAppService;

    @GetMapping("/{id}")
    @ApiOperation(value = "根据ID获取优惠券模板详情")
    public Result<CouponQueryVO> getTemplateById(@PathVariable Long id) {
        return Result.success(couponAppService.queryById(id));
    }
}

