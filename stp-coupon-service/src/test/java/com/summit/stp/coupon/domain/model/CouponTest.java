package com.summit.stp.coupon.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Coupon 领域模型单元测试（适用范围 + 状态动作）。
 */
class CouponTest {

    private Coupon.CouponBuilder base() {
        return Coupon.builder().id(1L).name("券").status(1);
    }

    @Test
    @DisplayName("isApplicable: scopeType 为 null -> 适用(null)")
    void isApplicable_nullScope() {
        assertThat(base().scopeType(null).build().isApplicable(1L, 2L)).isNull();
    }

    @Test
    @DisplayName("isApplicable: ALL_SCOPE -> 适用(null)")
    void isApplicable_allScope() {
        Coupon coupon = base().scopeType(Coupon.CouponScopeType.ALL_SCOPE).build();
        assertThat(coupon.isApplicable(1L, 2L)).isNull();
    }

    @Test
    @DisplayName("isApplicable: 范围ID为空 -> 优惠范围不适用")
    void isApplicable_emptyRelation() {
        Coupon coupon = base().scopeType(Coupon.CouponScopeType.SCOPE_OF_PRODUCT)
                .scopeRelationIds(List.of()).build();
        assertThat(coupon.isApplicable(1L, 2L)).isEqualTo("优惠范围不适用");
    }

    @Test
    @DisplayName("isApplicable: 指定商品分类 命中/未命中")
    void isApplicable_productType() {
        Coupon hit = base().scopeType(Coupon.CouponScopeType.SCOPE_OF_PRODUCT_TYPE)
                .scopeRelationIds(List.of(11L)).build();
        assertThat(hit.isApplicable(11L, 99L)).isNull();

        Coupon miss = base().scopeType(Coupon.CouponScopeType.SCOPE_OF_PRODUCT_TYPE)
                .scopeRelationIds(List.of(11L)).build();
        assertThat(miss.isApplicable(12L, 99L)).isEqualTo("优惠范围不包含该分类");
    }

    @Test
    @DisplayName("isApplicable: 指定商品 命中/未命中")
    void isApplicable_product() {
        Coupon hit = base().scopeType(Coupon.CouponScopeType.SCOPE_OF_PRODUCT)
                .scopeRelationIds(List.of(2001L)).build();
        assertThat(hit.isApplicable(11L, 2001L)).isNull();

        Coupon miss = base().scopeType(Coupon.CouponScopeType.SCOPE_OF_PRODUCT)
                .scopeRelationIds(List.of(2001L)).build();
        assertThat(miss.isApplicable(11L, 2002L)).isEqualTo("优惠券不适用于该单品");
    }

    @Test
    @DisplayName("ban()/active() 修改状态")
    void banAndActive() {
        Coupon coupon = base().build();
        coupon.ban();
        assertThat(coupon.getStatus()).isEqualTo(0);
        coupon.active();
        assertThat(coupon.getStatus()).isEqualTo(1);
    }
}
