package com.summit.stp.coupon.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.stp.coupon.domain.model.Coupon;
import com.summit.stp.coupon.infrastructure.persistence.po.CouponPO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * CouponRepositoryImpl 持久化映射单元测试：聚焦 toPO/toModel 的枚举 <-> code 双向转换。
 *
 * <p>通过测试子类暴露 protected 的 toPO/toModel；BaseMapper 使用 Mockito mock，
 * 转换逻辑本身不依赖 mapper，故可离线运行。</p>
 */
class CouponRepositoryImplTest {

    /** 暴露受保护转换方法的测试子类。 */
    static class TestableCouponRepository extends CouponRepositoryImpl {
        TestableCouponRepository(BaseMapper<CouponPO> baseMapper) {
            super(baseMapper);
        }

        public CouponPO toPo(Coupon domain) {
            return super.toPO(domain);
        }

        @Override
        public Coupon toModel(CouponPO po) {
            return super.toModel(po);
        }
    }

    private final TestableCouponRepository repository =
            new TestableCouponRepository(mock(BaseMapper.class));

    @Test
    @DisplayName("toModel: 枚举 code 反向转换为枚举对象")
    void toModel_enumConversion() {
        CouponPO po = CouponPO.builder()
                .id(1L).name("券").amount(new BigDecimal("10.00")).discount(new BigDecimal("0.9"))
                .status(1).type(1)
                .timeType(2).scopeType(3)
                .validDays(7).validHours(168)
                .build();

        Coupon coupon = repository.toModel(po);

        assertThat(coupon.getId()).isEqualTo(1L);
        assertThat(coupon.getTimeType()).isEqualTo(Coupon.CouponDateType.RECEIVE_EFFECT_TIME_PERIOD_BY_DAY);
        assertThat(coupon.getScopeType()).isEqualTo(Coupon.CouponScopeType.SCOPE_OF_PRODUCT);
        assertThat(coupon.getStatus()).isEqualTo(1);
    }

    @Test
    @DisplayName("toModel: timeType/scopeType 为 null -> null")
    void toModel_nullEnums() {
        CouponPO po = CouponPO.builder().id(2L).status(1).build();

        Coupon coupon = repository.toModel(po);

        assertThat(coupon.getTimeType()).isNull();
        assertThat(coupon.getScopeType()).isNull();
    }

    @Test
    @DisplayName("toModel: null 入参 -> null")
    void toModel_nullInput() {
        assertThat(repository.toModel(null)).isNull();
    }

    @Test
    @DisplayName("toPO: 枚举转换为 code")
    void toPo_enumConversion() {
        Coupon coupon = Coupon.builder()
                .id(3L).name("券").status(1)
                .timeType(Coupon.CouponDateType.RECEIVE_EFFECT_TIME_PERIOD_BY_HOUR)
                .scopeType(Coupon.CouponScopeType.SCOPE_OF_PRODUCT_TYPE)
                .build();

        CouponPO po = repository.toPo(coupon);

        assertThat(po.getId()).isEqualTo(3L);
        assertThat(po.getTimeType()).isEqualTo(3);
        assertThat(po.getScopeType()).isEqualTo(2);
        assertThat(po.getStatus()).isEqualTo(1);
    }

    @Test
    @DisplayName("toPO: 枚举为 null -> code 为 null")
    void toPo_nullEnums() {
        Coupon coupon = Coupon.builder().id(4L).name("券").status(1).build();

        CouponPO po = repository.toPo(coupon);

        assertThat(po.getTimeType()).isNull();
        assertThat(po.getScopeType()).isNull();
    }

    @Test
    @DisplayName("toPO: null 入参 -> null")
    void toPo_nullInput() {
        assertThat(repository.toPo(null)).isNull();
    }

    @Test
    @DisplayName("枚举 fromCode/getByCode 未知编码返回 null")
    void enums_unknownCode() {
        assertThat(Coupon.CouponScopeType.fromCode(99)).isNull();
        assertThat(Coupon.CouponDateType.getByCode(99)).isNull();
    }
}
