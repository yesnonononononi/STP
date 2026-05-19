package com.summit.stp.payment.infrastructure.persistence;

import com.summit.stp.payment.domain.model.Coupon;
import com.summit.stp.payment.domain.repository.CouponRepository;
import com.summit.stp.payment.infrastructure.persistence.mapper.CouponMapper;
import com.summit.stp.payment.infrastructure.persistence.po.CouponPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CouponRepositoryImpl implements CouponRepository {
    private final CouponMapper couponMapper;

    @Override
    public Coupon findCouponById(Long couponId) {
        CouponPO couponPO = couponMapper.findCouponById(couponId);
        if (couponPO == null) {
            return null;
        }
        return Coupon.of(
                couponPO.getId(),
                couponPO.getName(),
                couponPO.getAmount(),
                couponPO.getDiscount(),
                couponPO.getStatus()
        );
    }
}
