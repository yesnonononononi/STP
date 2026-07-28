package com.summit.stp.coupon.domain.model;

import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 优惠券 (领域模型)
 */
@Getter
@EqualsAndHashCode
@Builder
public class Coupon {
    private final Long id;
    private final String name;
    private final BigDecimal discount;
    private final BigDecimal amount;
    private  int status;
    private String description;
    /**
     * 可用范围类型：1-全场通用，2-指定商品分类，3-指定商品
     */
    private final CouponScopeType scopeType;
    /**
     * 适用范围内的分类或商品ID列表
     */
    private List<Long> scopeRelationIds;
    /**
     * 有效时间类型 1-固定时间段 2-领取后生效(按天) 3-领取后生效(按小时)
     */
    private CouponDateType timeType;
    /**
     * 创建时间
     */
    private Timestamp createTime;
    /**
     * 更新时间
     */
    private Timestamp updateTime;
    /**
     * 优惠券有效期有效天数
     */
    private Integer validDays;
    /**
     * 优惠券有效期有效小时数
     */
    private Integer validHours;

    @Getter
    @AllArgsConstructor
     public enum CouponDateType{
        FIXED_TIME_PERIOD(1),
        RECEIVE_EFFECT_TIME_PERIOD_BY_DAY(2),
        RECEIVE_EFFECT_TIME_PERIOD_BY_HOUR(3);
        private final Integer code;
        public static CouponDateType getByCode(Integer code) {
            for (CouponDateType value : values()) {
                if (value.code.equals(code)) {
                    return value;
                }
            }
            return null;
        }
    }
    @Getter
    @AllArgsConstructor
    public enum CouponScopeType{
        ALL_SCOPE(1),
        SCOPE_OF_PRODUCT_TYPE(2),
        SCOPE_OF_PRODUCT(3);
        private final Integer code;
        public static CouponScopeType fromCode(Integer code) {
            for (CouponScopeType value : values()) {
                if (value.code.equals(code)) {
                    return value;
                }
            }
            return null;
        }
    }

    /**
     * 校验优惠券是否适用于指定商品分类和商品单品
     * 
     * @param productTypeId 商品分类ID
     * @param packageId 商品单品ID
     * @return 是否适用
     */
    public String isApplicable(Long productTypeId, Long packageId) {
        // 1. 全场通用
        if (this.scopeType == null || this.scopeType == CouponScopeType.ALL_SCOPE) {
            return null;
        }

        if (this.scopeRelationIds == null || this.scopeRelationIds.isEmpty()) {
            return "优惠范围不适用";
        }

        // 2. 指定商品分类
        if (this.scopeType == CouponScopeType.SCOPE_OF_PRODUCT_TYPE) {
            return this.scopeRelationIds.contains(productTypeId) ? null : "优惠范围不包含该分类";
        }

        // 3. 指定商品 (单品)
        if (this.scopeType == CouponScopeType.SCOPE_OF_PRODUCT) {
            return this.scopeRelationIds.contains(packageId) ? null : "优惠券不适用于该单品";
        }



        return "优惠范围不适用";
    }

    /**
     * 禁用优惠券
     */
    public void ban(){
        this.status = 0;
    }

    /**
     * 启用优惠券
     */
    public void active(){
        this.status = 1;
    }
}
