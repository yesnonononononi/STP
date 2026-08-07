package com.summit.stp.common.feign;

import com.summit.stp.common.application.api.vo.CouponQueryVO;
import com.summit.stp.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@FeignClient(name = "stp-coupon-service", contextId = "couponFeignClient")
public interface CouponFeignClient {

    @GetMapping("/coupon/internal/get/{id}")
    Result<CouponQueryVO> queryById(@PathVariable("id") Long id);

    @PostMapping("/coupon/internal/use")
    Result<Void> use(@RequestParam("id") Long id, @RequestParam("orderId") Long orderId, @RequestParam("typeId") Long typeId, @RequestParam("packageId") Long packageId);

    @PostMapping("/coupon/internal/refund")
    Result<Void> refund(@RequestParam("id") Long id);

    @GetMapping("/coupon/internal/calculate")
    Result<BigDecimal> calculateAmount(
            @RequestParam("price") BigDecimal price,
            @RequestParam("quantity") Integer quantity,
            @RequestParam(value = "couponId", required = false) Long couponId);

    @PostMapping("/coupon/internal/batch")
    Result<Map<Long, CouponQueryVO>> queryByIds(@RequestBody List<Long> cList);
}
