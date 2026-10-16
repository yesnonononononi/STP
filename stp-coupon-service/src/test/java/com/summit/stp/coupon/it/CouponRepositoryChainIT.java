package com.summit.stp.coupon.it;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.coupon.domain.model.Coupon;
import com.summit.stp.coupon.domain.repository.CouponRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 优惠券仓储链路集成测试（真实 MySQL）。
 *
 * <p>覆盖 {@code CouponRepositoryImpl.findById / findByIds} 读取 {@code coupon} 表，
 * 并完成 PO -> Domain 的枚举（scopeType/timeType）转换。</p>
 *
 * <p>注意：{@code CouponRepositoryImpl.toModel} 未回填 {@code scopeRelationIds}（已知），
 * 因此此处不断言该字段。</p>
 */
@Tag("integration")
class CouponRepositoryChainIT extends AbstractCouponIntegrationTest {

    @Autowired
    private CouponRepository couponRepository;

    private Coupon newCoupon(String name, Coupon.CouponScopeType scopeType, Coupon.CouponDateType timeType) {
        return Coupon.builder()
                .name(name)
                .discount(new BigDecimal("0.90"))
                .amount(new BigDecimal("10.00"))
                .type(1)
                .status(1)
                .description("desc")
                .scopeType(scopeType)
                .timeType(timeType)
                .validDays(7)
                .validHours(168)
                .build();
    }

    private Coupon saveAndFetch(Coupon coupon) {
        couponRepository.save(coupon);
        Page<Coupon> page = couponRepository.list(coupon.getName(), null, 1, 10);
        assertThat(page.getRecords()).isNotEmpty();
        return page.getRecords().stream()
                .filter(c -> coupon.getName().equals(c.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("未回读到刚保存的优惠券: " + coupon.getName()));
    }

    @Test
    @DisplayName("save -> findById：字段与 scopeType/timeType 枚举转换正确")
    void saveAndFindById_mapping() {
        Coupon persisted = saveAndFetch(newCoupon("IT-单品券-A", Coupon.CouponScopeType.SCOPE_OF_PRODUCT,
                Coupon.CouponDateType.RECEIVE_EFFECT_TIME_PERIOD_BY_DAY));

        assertThat(persisted.getId()).isNotNull();

        Optional<Coupon> found = couponRepository.findById(persisted.getId());
        assertThat(found).isPresent();
        Coupon coupon = found.get();
        assertThat(coupon.getId()).isEqualTo(persisted.getId());
        assertThat(coupon.getName()).isEqualTo("IT-单品券-A");
        assertThat(coupon.getAmount()).isEqualByComparingTo("10.00");
        assertThat(coupon.getDiscount()).isEqualByComparingTo("0.90");
        assertThat(coupon.getType()).isEqualTo(1);
        assertThat(coupon.getStatus()).isEqualTo(1);
        assertThat(coupon.getValidDays()).isEqualTo(7);
        assertThat(coupon.getValidHours()).isEqualTo(168);
        assertThat(coupon.getDescription()).isEqualTo("desc");
        assertThat(coupon.getScopeType()).isEqualTo(Coupon.CouponScopeType.SCOPE_OF_PRODUCT);
        assertThat(coupon.getTimeType()).isEqualTo(Coupon.CouponDateType.RECEIVE_EFFECT_TIME_PERIOD_BY_DAY);
    }

    @Test
    @DisplayName("findById：不存在的 ID 返回 Optional.empty")
    void findById_notExist() {
        assertThat(couponRepository.findById(999_999_999L)).isEmpty();
    }

    @Test
    @DisplayName("findByIds：批量读取并各自完成枚举转换")
    void findByIds_batch() {
        Coupon allScope = saveAndFetch(newCoupon("IT-全场券-B", Coupon.CouponScopeType.ALL_SCOPE,
                Coupon.CouponDateType.RECEIVE_EFFECT_TIME_PERIOD_BY_HOUR));
        Coupon productType = saveAndFetch(newCoupon("IT-分类券-C", Coupon.CouponScopeType.SCOPE_OF_PRODUCT_TYPE,
                Coupon.CouponDateType.FIXED_TIME_PERIOD));

        List<Coupon> coupons = couponRepository.findByIds(List.of(allScope.getId(), productType.getId()));

        assertThat(coupons).hasSize(2);
        Coupon loadedAll = coupons.stream().filter(c -> c.getId().equals(allScope.getId())).findFirst().orElseThrow();
        Coupon loadedType = coupons.stream().filter(c -> c.getId().equals(productType.getId())).findFirst().orElseThrow();
        assertThat(loadedAll.getScopeType()).isEqualTo(Coupon.CouponScopeType.ALL_SCOPE);
        assertThat(loadedAll.getTimeType()).isEqualTo(Coupon.CouponDateType.RECEIVE_EFFECT_TIME_PERIOD_BY_HOUR);
        assertThat(loadedType.getScopeType()).isEqualTo(Coupon.CouponScopeType.SCOPE_OF_PRODUCT_TYPE);
        assertThat(loadedType.getTimeType()).isEqualTo(Coupon.CouponDateType.FIXED_TIME_PERIOD);
    }
}
