package com.summit.stp.coupon.domain.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.coupon.domain.model.UserCoupon;
import java.util.List;

public interface UserCouponRepository {
    /**
     * 根据实例 ID 查询用户优惠券
     */
    UserCoupon findUserCouponById(Long id);

    /**
     * 保存或更新用户优惠券状态
     */
    void save(UserCoupon userCoupon);

    /**
     * 分页查询特定用户的个人优惠券历史记录
     */
    Page<UserCoupon> queryHistoryByUser(Long userId, long page, long pageSize);

    /**
     * 查询特定用户持有的所有未使用优惠券
     */
    List<UserCoupon> findUnusedByUserId(Long userId);
}
