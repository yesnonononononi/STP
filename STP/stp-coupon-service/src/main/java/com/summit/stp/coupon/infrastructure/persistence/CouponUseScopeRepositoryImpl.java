package com.summit.stp.coupon.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.summit.stp.coupon.domain.model.CouponUseScope;
import com.summit.stp.coupon.domain.repository.CouponUseScopeRepository;
import com.summit.stp.coupon.infrastructure.persistence.mapper.CouponUseScopeMapper;
import com.summit.stp.coupon.infrastructure.persistence.po.CouponUseScopePO;
import org.springframework.stereotype.Repository;

@Repository
public class CouponUseScopeRepositoryImpl implements CouponUseScopeRepository {
    private final CouponUseScopeMapper couponUseScopeMapper;

    public CouponUseScopeRepositoryImpl(CouponUseScopeMapper couponUseScopeMapper) {
        this.couponUseScopeMapper = couponUseScopeMapper;
    }


    @Override
    public CouponUseScope findByCouponId(Long couponId) {
        CouponUseScopePO couponUseScopePO = couponUseScopeMapper.selectOne(new LambdaQueryWrapper<CouponUseScopePO>().eq(CouponUseScopePO::getCouponId, couponId));
        return toDomain(couponUseScopePO);
    }

    private CouponUseScope toDomain(CouponUseScopePO couponUseScopePO) {
        return CouponUseScope.builder()
                .id(couponUseScopePO.getId())
                .couponId(couponUseScopePO.getCouponId())
                .relationId(couponUseScopePO.getRelationId())
                .build();
    }
}
