package com.summit.stp.coupon.application.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.coupon.application.vo.CouponQueryVO;

import java.math.BigDecimal;

public interface CouponAppService {
    /**
     * 查询优惠券
     * @param id 优惠券id
     * @return 视图对象
     */
    CouponQueryVO queryById(Long id);

    /**
     * 查询历史优惠券
     * @param page 页码
     * @param pageSize 每页数量
     * @return 视图对象集合
     */
    Page<CouponQueryVO> queryHistory(long page,long pageSize);

    /**
     * 优惠券保存
     * @param id 优惠券id
     */
    void save(Long id);


    /**
     * 优惠券使用
     * @param id 优惠券id
     * @param orderId 关联订单id
     */
    void use(Long id, Long orderId);

    /**
     * 优惠券退回
     * @param id 优惠券id
     */

    void refund(Long id);


    /**
     * 计算订单金额
      * @param price 商品单价
      * @param quantity 数量
      * @return 订单金额
     */
    BigDecimal calculateAmount(BigDecimal price, Integer quantity,Long couponId);
}
