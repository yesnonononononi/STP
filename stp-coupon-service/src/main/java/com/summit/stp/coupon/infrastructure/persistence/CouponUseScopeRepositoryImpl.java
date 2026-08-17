package com.summit.stp.coupon.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.summit.devframeworkdddstarter.repo.AbstractRepository;
import com.summit.stp.coupon.domain.model.CouponUseScope;
import com.summit.stp.coupon.domain.repository.CouponUseScopeRepository;
import com.summit.stp.coupon.infrastructure.persistence.po.CouponUseScopePO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class CouponUseScopeRepositoryImpl extends AbstractRepository<CouponUseScope, CouponUseScopePO> implements CouponUseScopeRepository {

    public CouponUseScopeRepositoryImpl(BaseMapper<CouponUseScopePO> baseMapper) {
        super(baseMapper);
    }

    @Override
    public CouponUseScope findByCouponId(Long couponId) {
        return findBy(couponId, CouponUseScopePO::getCouponId).orElse(null);
    }

    @Override
    public Map<Long, CouponUseScope> findByCouponIds(List<Long> couponIds) {
        if (couponIds == null || couponIds.isEmpty()) {
            return Map.of();
        }
        return getBaseMapper().selectList(new LambdaQueryWrapper<CouponUseScopePO>()
                        .in(CouponUseScopePO::getCouponId, couponIds))
                .stream()
                .map(this::toModel)
                .collect(Collectors.toMap(CouponUseScope::getCouponId, Function.identity(), (first, ignored) -> first));
    }

    @Override
    protected CouponUseScopePO toPO(CouponUseScope domain) {
        if (domain == null) return null;
        return CouponUseScopePO.builder()
                .id(domain.getId())
                .couponId(domain.getCouponId())
                .relationId(domain.getRelationId())
                .build();
    }

    @Override
    protected CouponUseScope toModel(CouponUseScopePO couponUseScopePO) {
        if (couponUseScopePO == null) return null;
        return CouponUseScope.builder()
                .id(couponUseScopePO.getId())
                .couponId(couponUseScopePO.getCouponId())
                .relationId(couponUseScopePO.getRelationId())
                .build();
    }
}

