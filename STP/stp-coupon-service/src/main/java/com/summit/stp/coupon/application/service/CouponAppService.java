package com.summit.stp.coupon.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.coupon.application.dto.CouponActivityDTO;
import com.summit.stp.coupon.application.vo.CouponActivityQueryVO;
import com.summit.stp.coupon.domain.model.CouponStatus;
import com.summit.stp.common.application.vo.CouponQueryVO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface CouponAppService {
    /**
     * 查询当前用户可用的未使用优惠券
     */
    List<CouponQueryVO> queryAvailableCoupons();


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
    Page<CouponQueryVO> queryHistory(long page, long pageSize, CouponStatus status);

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

    /**
     * 下单前置强校验：验证优惠券是否适用于指定商品分类和商品套餐单品
     * 
     * @param couponId 优惠券ID
     * @param typeId 商品套餐分类ID
     * @param packageId 具体商品套餐单品ID
     */
    void validateCouponApplicability(Long couponId, Long typeId, Long packageId);

    /**
     * 查询当前用户的所有优惠券，并根据指定的商品分类和单品标识在当前订单下是否可用
     * 
     * @param typeId 商品套餐分类ID
     * @param packageId 具体商品套餐单品ID
     * @return 包含可用性标识的优惠券VO列表
     */
    List<CouponQueryVO> queryCouponsForOrder(Long typeId, Long packageId);

    /**
     * 查询所有可用的优惠券投放活动列表
     * 
     * @return 优惠券活动VO列表
     */
    List<CouponActivityQueryVO> queryAllActivitiesByScopeType(Integer scopeType);

    /**
     * 用户根据活动ID领取优惠券
     * 
     * @param activityId 优惠券投放活动ID
     */
    void receiveActivityCoupon(Long activityId);

    /**
     * 新增优惠券投放活动
     * 
     * @param dto 优惠券活动DTO
     */
    void saveActivity(CouponActivityDTO dto);

    /**
     * 更新优惠券投放活动
     * 
     * @param dto 优惠券活动DTO
     */
    void updateActivity(CouponActivityDTO dto);

    /**
     * 删除指定的优惠券投放活动
     * 
     * @param id 优惠券投放活动ID
     */
    void deleteActivity(Long id);

    Map<Long, CouponQueryVO> queryByIds(List<Long> cList);
}
