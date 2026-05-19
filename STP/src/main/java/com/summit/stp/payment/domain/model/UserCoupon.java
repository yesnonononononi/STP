package com.summit.stp.payment.domain.model;

import com.summit.stp.payment.infrastructure.Enum.CouponStatus;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.sql.Timestamp;

/**
 * 用户拥有的优惠券实例 (领域模型，聚合根/实体)
 */
@Getter
@EqualsAndHashCode
public class UserCoupon {
    private final Long id;
    private final Long userId;
    private final Long couponTemplateId; // 关联的优惠券模板 ID
    private CouponStatus status; // 使用状态 (NOT_USE, USE, EXPIRED)
    private final Timestamp createTime;
    private Timestamp updateTime;
    
    // 关联的优惠券模板详情 (在领域层作为只读关联，通过仓储或应用服务装配)
    private Coupon template;

    public UserCoupon(Long id, Long userId, Long couponTemplateId, int statusCode, Timestamp createTime, Timestamp updateTime) {
        this.id = id;
        this.userId = userId;
        this.couponTemplateId = couponTemplateId;
        this.status = CouponStatus.fromCode(statusCode);
        this.createTime = createTime != null ? createTime : new Timestamp(System.currentTimeMillis());
        this.updateTime = updateTime != null ? updateTime : this.createTime;
    }

    public static UserCoupon of(Long id, Long userId, Long couponTemplateId, Integer statusCode, Timestamp createTime, Timestamp updateTime) {
        return new UserCoupon(
                id,
                userId,
                couponTemplateId,
                statusCode != null ? statusCode : 1, // 默认 1 - 未使用
                createTime,
                updateTime
        );
    }

    /**
     * 绑定静态优惠券模板详情
     */
    public void bindTemplate(Coupon template) {
        this.template = template;
    }

    /**
     * 使用优惠券
     */
    public void use() {
        if (!isAvailable()) {
            throw new IllegalStateException("优惠券不可用，当前状态为: " + status.getDescription());
        }
        this.status = CouponStatus.USE;
        this.updateTime = new Timestamp(System.currentTimeMillis());
    }

    /**
     * 是否可用
     */
    public boolean isAvailable() {
        return this.status == CouponStatus.NOT_USE;
    }
}
