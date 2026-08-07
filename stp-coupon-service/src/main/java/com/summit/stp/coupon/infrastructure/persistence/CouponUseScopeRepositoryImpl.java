package com.summit.stp.coupon.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.summit.stp.coupon.domain.model.CouponUseScope;
import com.summit.stp.coupon.domain.repository.CouponUseScopeRepository;
import com.summit.stp.coupon.infrastructure.persistence.mapper.CouponUseScopeMapper;
import com.summit.stp.coupon.infrastructure.persistence.po.CouponUseScopePO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class CouponUseScopeRepositoryImpl implements CouponUseScopeRepository {
    private final CouponUseScopeMapper couponUseScopeMapper;

    public CouponUseScopeRepositoryImpl(CouponUseScopeMapper couponUseScopeMapper) {
        this.couponUseScopeMapper = couponUseScopeMapper;
    }


    @Override
    public CouponUseScope findByCouponId(Long couponId) {
        CouponUseScopePO couponUseScopePO = couponUseScopeMapper.selectOne(new LambdaQueryWrapper<CouponUseScopePO>().eq(CouponUseScopePO::getCouponId, couponId));
        if (couponUseScopePO == null) {
            return null;
        }
        return toDomain(couponUseScopePO);
    }

    @Override
    public Map<Long, CouponUseScope> findByCouponIds(List<Long> couponIds) {
        if (couponIds == null || couponIds.isEmpty()) {
            return Map.of();
        }
        return couponUseScopeMapper.selectList(new LambdaQueryWrapper<CouponUseScopePO>()
                        .in(CouponUseScopePO::getCouponId, couponIds))
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toMap(CouponUseScope::getCouponId, Function.identity(), (first, ignored) -> first));
    }

    private CouponUseScope toDomain(CouponUseScopePO couponUseScopePO) {
        return CouponUseScope.builder()
                .id(couponUseScopePO.getPublicId())
                .couponId(couponUseScopePO.getCouponId())
                .relationId(couponUseScopePO.getRelationId())
                .build();
    }
}
