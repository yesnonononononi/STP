package com.summit.stp.coupon.it;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.coupon.api.vo.CouponQueryVO;
import com.summit.stp.coupon.application.service.CouponAppService;
import com.summit.stp.coupon.domain.exception.NoSuchCouponException;
import com.summit.stp.coupon.domain.model.Coupon;
import com.summit.stp.coupon.domain.repository.CouponRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 优惠券查询应用服务集成测试（真实 MySQL）。
 *
 * <p>覆盖 {@code CouponAppServiceImpl.queryById / queryByIds} 基于真实仓储返回正确 VO，
 * 含 scopeType 与 reason（适用范围判定）映射。</p>
 */
@Tag("integration")
class CouponQueryServiceIT extends AbstractCouponIntegrationTest {

    @Autowired
    private CouponAppService couponAppService;

    @Autowired
    private CouponRepository couponRepository;

    private Coupon saveAndFetch(String name, Coupon.CouponScopeType scopeType) {
        Coupon coupon = Coupon.builder()
                .name(name)
                .amount(new BigDecimal("20.00"))
                .discount(new BigDecimal("0.80"))
                .type(1)
                .status(1)
                .description("query-it")
                .scopeType(scopeType)
                .timeType(Coupon.CouponDateType.RECEIVE_EFFECT_TIME_PERIOD_BY_DAY)
                .validDays(30)
                .validHours(720)
                .build();
        couponRepository.save(coupon);
        Page<Coupon> page = couponRepository.list(name, null, 1, 10);
        assertThat(page.getRecords()).isNotEmpty();
        return page.getRecords().stream()
                .filter(c -> name.equals(c.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("未回读到刚保存的优惠券: " + name));
    }

    @Test
    @DisplayName("queryById：全场券 -> scopeType=1 且 reason 为空")
    void queryById_allScope() {
        Coupon saved = saveAndFetch("IT-查询-全场券", Coupon.CouponScopeType.ALL_SCOPE);

        CouponQueryVO vo = couponAppService.queryById(saved.getId());

        assertThat(vo.getId()).isEqualTo(saved.getId());
        assertThat(vo.getName()).isEqualTo("IT-查询-全场券");
        assertThat(vo.getStatus()).isEqualTo(1);
        assertThat(vo.getValidDays()).isEqualTo(30);
        assertThat(vo.getScopeType()).isEqualTo(1);
        assertThat(vo.getReason()).isNull();
    }

    @Test
    @DisplayName("queryById：指定商品券 -> scopeType=3 且 reason 提示范围不适用")
    void queryById_scoped() {
        Coupon saved = saveAndFetch("IT-查询-单品券", Coupon.CouponScopeType.SCOPE_OF_PRODUCT);

        CouponQueryVO vo = couponAppService.queryById(saved.getId());

        assertThat(vo.getScopeType()).isEqualTo(3);
        assertThat(vo.getReason()).isEqualTo("优惠范围不适用");
    }

    @Test
    @DisplayName("queryById：不存在 -> NoSuchCouponException")
    void queryById_notExist() {
        assertThatThrownBy(() -> couponAppService.queryById(999_999_999L))
                .isInstanceOf(NoSuchCouponException.class);
    }

    @Test
    @DisplayName("queryByIds：批量返回按 id 索引的 VO")
    void queryByIds_batch() {
        Coupon a = saveAndFetch("IT-查询-批量A", Coupon.CouponScopeType.ALL_SCOPE);
        Coupon b = saveAndFetch("IT-查询-批量B", Coupon.CouponScopeType.SCOPE_OF_PRODUCT_TYPE);

        Map<Long, CouponQueryVO> result = couponAppService.queryByIds(List.of(a.getId(), b.getId()));

        assertThat(result).containsKeys(a.getId(), b.getId());
        assertThat(result.get(a.getId()).getScopeType()).isEqualTo(1);
        assertThat(result.get(b.getId()).getScopeType()).isEqualTo(2);
    }

    @Test
    @DisplayName("queryByIds：空集合 -> 空 Map")
    void queryByIds_empty() {
        assertThat(couponAppService.queryByIds(List.of())).isEmpty();
    }
}
