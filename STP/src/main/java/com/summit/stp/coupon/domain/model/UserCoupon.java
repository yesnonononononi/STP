package com.summit.stp.coupon.domain.model;

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
    private Long orderId;        // 关联核销的订单 ID
    private Timestamp usedTime;  // 核销时间
    private final Timestamp createTime;
    private Timestamp updateTime;
    
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
            throw new IllegalStateException("优惠券不可用，当前状态为: " + status.getDescription());
        }
        this.status = CouponStatus.USE;
        this.orderId = orderId;
        this.usedTime = new Timestamp(System.currentTimeMillis());
        this.updateTime = this.usedTime;
    }

    /**
     * 是否可用
     */
    public boolean isAvailable() {
        return this.status == CouponStatus.NOT_USE;
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
