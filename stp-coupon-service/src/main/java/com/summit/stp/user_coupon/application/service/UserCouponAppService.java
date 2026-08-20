package com.summit.stp.user_coupon.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.coupon.api.vo.CouponQueryVO;
import com.summit.stp.user_coupon.domain.model.CouponStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface UserCouponAppService {
    /**
     * 查询当前用户可用的未使用优惠券
     */
    List<CouponQueryVO> queryAvailableCoupons();

    /**
     * 查询用户优惠券详情
     */
    CouponQueryVO queryById(Long id);


    /**
     * 分页查询当前用户的个人优惠券历史记录
     */
    Page<CouponQueryVO> queryHistory(long page, long pageSize, CouponStatus status);

    /**
     * 核销使用优惠券
     */
    void use(Long id, Long orderId, Long typeId, Long packageId);

    /**
     * 退还优惠券
     */
    void refund(Long id);

    /**
     * 订单优惠金额计算
     */
    BigDecimal calculateAmount(BigDecimal price, Integer quantity, Long couponId);

    /**
     * 查询当前用户的所有优惠券，并根据指定的商品分类和单品标识在当前订单下是否可用
     */
    List<CouponQueryVO> queryCouponsForOrder(Long typeId, Long packageId);
}

