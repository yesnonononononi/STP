package com.summit.stp.common.feign;

import com.summit.stp.common.application.vo.CouponQueryVO;
import com.summit.stp.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@FeignClient(name = "stp-coupon-service", contextId = "couponFeignClient")
public interface CouponFeignClient {

    @GetMapping("/coupon/internal/get/{id}")
    Result<CouponQueryVO> queryById(@PathVariable("id") Long id);

    @PostMapping("/coupon/internal/use")
    Result<Void> use(@RequestParam("id") Long id, @RequestParam("orderId") Long orderId);

    @PostMapping("/coupon/internal/refund")
    Result<Void> refund(@RequestParam("id") Long id);

    @GetMapping("/coupon/internal/calculate")
    Result<BigDecimal> calculateAmount(
            @RequestParam("price") BigDecimal price,
            @RequestParam("quantity") Integer quantity,
            @RequestParam(value = "couponId", required = false) Long couponId);

    @PostMapping("/coupon/internal/validate")
    Result<Void> validateCouponApplicability(@RequestParam("couponId") Long couponId, @RequestParam("typeId") Long typeId, @RequestParam("packageId") Long packageId);

    @PostMapping("/coupon/internal/batch")
    Result<Map<Long, CouponQueryVO>> queryByIds(@RequestBody List<Long> cList);
}
