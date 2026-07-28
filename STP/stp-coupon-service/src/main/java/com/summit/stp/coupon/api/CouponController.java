package com.summit.stp.coupon.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.coupon.application.dto.CouponActivityDTO;
import com.summit.stp.coupon.application.service.CouponAppService;
import com.summit.stp.coupon.application.vo.CouponActivityQueryVO;
import com.summit.stp.coupon.domain.model.CouponStatus;
import com.summit.stp.common.application.vo.CouponQueryVO;
import com.summit.stp.common.result.Result;
import com.summit.stp.common.annotation.Login;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Login
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
            @RequestParam(defaultValue = "10") long pageSize,
    @RequestParam(required = false) Integer status
    ) {
        return Result.success(couponAppService.queryHistory(page, pageSize,status == null ? null :CouponStatus.fromCode(status)));
    }

    /**
     * 根据商品分类ID和具体套餐ID获取用户所有优惠券并标识本单是否可用
     *
     * @param typeId    商品分类ID
     * @param packageId 具体套餐ID
     * @return 包含可用性标识的优惠券列表
     */
    @GetMapping("/order-list")
    public Result<List<CouponQueryVO>> getCouponsForOrder(
            @RequestParam("typeId") Long typeId,
            @RequestParam("packageId") Long packageId) {
        return Result.success(couponAppService.queryCouponsForOrder(typeId, packageId));
    }

    /**
     * 获取所有可供领取的优惠券投放活动列表
     *
     * @return 包含关联优惠券模板信息的活动VO列表
     */
    @GetMapping("/activity/list/{type}")
    public Result<List<CouponActivityQueryVO>> getCouponActivitiesByScopeType(@PathVariable(required = true) Integer type) {
        return Result.success(couponAppService.queryAllActivitiesByScopeType(type));
    }

    /**
     * 领取活动优惠券
     *
     * @param activityId 优惠券投放活动ID
     * @return 操作结果
     */
    @GetMapping("/activity/receive/{activityId}")
    public Result<Void> receiveCoupon(@PathVariable Long activityId) {
        couponAppService.receiveActivityCoupon(activityId);
        return Result.success();
    }

    /**
     * 新增优惠券投放活动
     *
     * @param dto 优惠券活动数据DTO
     * @return 操作结果
     */
    @PostMapping("/activity")
    public Result<Void> addActivity(@RequestBody CouponActivityDTO dto) {
        couponAppService.saveActivity(dto);
        return Result.success();
    }

    /**
     * 更新优惠券投放活动
     *
     * @param dto 优惠券活动数据DTO
     * @return 操作结果
     */
    @PutMapping("/activity")
    public Result<Void> updateActivity(@RequestBody CouponActivityDTO dto) {
        couponAppService.updateActivity(dto);
        return Result.success();
    }

    /**
     * 删除指定的优惠券投放活动
     *
     * @param id 优惠券投放活动ID
     * @return 操作结果
     */
    @DeleteMapping("/activity/{id}")
    public Result<Void> deleteActivity(@PathVariable("id") Long id) {
        couponAppService.deleteActivity(id);
        return Result.success();
    }

}
