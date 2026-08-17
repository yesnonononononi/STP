package com.summit.stp.coupon.application.service.impl;

import com.summit.stp.coupon.api.vo.CouponQueryVO;
import com.summit.stp.coupon.application.service.CouponAppService;
import com.summit.stp.coupon.domain.model.Coupon;
import com.summit.stp.coupon.domain.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponAppServiceImpl implements CouponAppService {
    private final CouponRepository couponRepository;

    @Override
    public CouponQueryVO queryById(Long id) {
        Coupon coupon = couponRepository.findCouponById(id);
        if (coupon == null) {
            return null;
        }
        return toVO(coupon);
    }

    @Override
    public Map<Long, CouponQueryVO> queryByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        List<Coupon> byIds = couponRepository.findByIds(ids);
        return byIds.stream().collect(Collectors.toMap(Coupon::getId, this::toVO));
    }

    private CouponQueryVO toVO(Coupon coupon) {
        return CouponQueryVO.builder()
                .id(coupon.getId())
                .name(coupon.getName())
                .discount(coupon.getDiscount())
                .amount(coupon.getAmount())
                .status(coupon.getStatus())
                .validDays(coupon.getValidDays())
                .validHours(coupon.getValidHours())
                .scopeType(coupon.getScopeType() != null ? coupon.getScopeType().getCode() : null)
                .reason(coupon.isApplicable(null, null))
                .description(coupon.getDescription())
                .build();
    }
}

