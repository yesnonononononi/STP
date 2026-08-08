package com.summit.stp.user_coupon.domain.model;

import com.summit.stp.coupon.domain.model.Coupon;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.sql.Timestamp;

/**
 * 用户拥有的优惠券实例 (领域模型，聚合根/实体)
 */
@Getter
@EqualsAndHashCode
@Builder
public class UserCoupon {
    private final Long id;
    private final Long userId;
    private final Long couponTemplateId; // 关联的优惠券模板 ID
    private CouponStatus status; // 使用状态 (NOT_USE, USE, EXPIRED)
    private Timestamp usedTime;  // 核销时间
    private final Timestamp createTime;
    private Timestamp updateTime;
    private Timestamp endTime;
    private Long orderId; // 关联的订单 ID

    // 关联的优惠券模板详情 (在领域层作为只读关联，通过仓储或应用服务装配)
    private Coupon template;

    /**
     * 绑定静态优惠券模板详情
     */
    public void bindTemplate(Coupon template) {
        this.template = template;
    }

    /**
     * 使用优惠券
     */
    public void use(Long orderId) {
        if (!isAvailable()) {
            throw new IllegalStateException("优惠券不可用，当前状态为: " + (status != null ? status.getDescription() : "未知"));
        }
        if (orderId == null) {
            throw new IllegalArgumentException("非法使用,未找到订单信息");
        }
        this.status = CouponStatus.USE;
        this.usedTime = new Timestamp(System.currentTimeMillis());
        this.updateTime = this.usedTime;
        this.orderId = orderId;
    }

    /**
     * 是否可用：需同时满足状态为未使用、未记录核销时间、且未超过过期时间
     */
    public boolean isAvailable() {
        if (this.status != CouponStatus.NOT_USE) {
            return false;
        }
        if (this.usedTime != null) {
            return false;
        }
        if (this.endTime != null && System.currentTimeMillis() > this.endTime.getTime()) {
            return false;
        }
        return true;
    }

    /**
     * 退回优惠券
     */
    public void refund() {
        this.status = CouponStatus.NOT_USE;
        this.orderId = null;
        this.usedTime = null;
        this.updateTime = new Timestamp(System.currentTimeMillis());
    }
}
