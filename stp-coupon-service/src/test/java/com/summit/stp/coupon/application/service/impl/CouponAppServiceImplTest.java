package com.summit.stp.coupon.application.service.impl;

import com.summit.stp.coupon.api.vo.CouponQueryVO;
import com.summit.stp.coupon.domain.exception.NoSuchCouponException;
import com.summit.stp.coupon.domain.model.Coupon;
import com.summit.stp.coupon.domain.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * CouponAppServiceImpl 纯单元测试。
 */
@ExtendWith(MockitoExtension.class)
class CouponAppServiceImplTest {

    @Mock
    private CouponRepository couponRepository;

    private CouponAppServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CouponAppServiceImpl(couponRepository);
    }

    private Coupon coupon(Long id) {
        return Coupon.builder()
                .id(id)
                .name("满减券")
                .discount(new BigDecimal("0.9"))
                .amount(new BigDecimal("10.00"))
                .status(1)
                .validDays(7)
                .validHours(168)
                .description("desc")
                .build();
    }

    @Test
    @DisplayName("queryById: 存在 -> 返回 VO")
    void queryById_found() {
        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon(1L)));

        CouponQueryVO vo = service.queryById(1L);

        assertThat(vo.getId()).isEqualTo(1L);
        assertThat(vo.getName()).isEqualTo("满减券");
        assertThat(vo.getStatus()).isEqualTo(1);
        assertThat(vo.getValidDays()).isEqualTo(7);
        assertThat(vo.getScopeType()).isNull();
        assertThat(vo.getReason()).isNull();
        assertThat(vo.getDescription()).isEqualTo("desc");
    }

    @Test
    @DisplayName("queryById: 不存在 -> NoSuchCouponException")
    void queryById_notFound() {
        when(couponRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.queryById(2L))
                .isInstanceOf(NoSuchCouponException.class);
    }

    @Test
    @DisplayName("queryByIds: null/空 -> Map.of()")
    void queryByIds_empty() {
        assertThat(service.queryByIds(null)).isEmpty();
        assertThat(service.queryByIds(List.of())).isEmpty();
    }

    @Test
    @DisplayName("queryByIds: 非空 -> 按 id 映射")
    void queryByIds_nonEmpty() {
        when(couponRepository.findByIds(List.of(1L, 2L)))
                .thenReturn(List.of(coupon(1L), coupon(2L)));

        Map<Long, CouponQueryVO> result = service.queryByIds(List.of(1L, 2L));

        assertThat(result).hasSize(2);
        assertThat(result).containsKeys(1L, 2L);
        assertThat(result.get(1L).getName()).isEqualTo("满减券");
    }

    @Test
    @DisplayName("toVO: scopeType 与 reason 映射")
    void queryById_scopeMapping() {
        Coupon scoped = Coupon.builder()
                .id(3L).name("单品券").status(1)
                .scopeType(Coupon.CouponScopeType.SCOPE_OF_PRODUCT)
                .build();
        when(couponRepository.findById(3L)).thenReturn(Optional.of(scoped));

        CouponQueryVO vo = service.queryById(3L);

        assertThat(vo.getScopeType()).isEqualTo(3);
        // scopeRelationIds 为空 -> isApplicable 返回“优惠范围不适用”
        assertThat(vo.getReason()).isEqualTo("优惠范围不适用");
    }
}
