package com.summit.stp.coupon.infrastructure.persistence;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.summit.stp.coupon.domain.model.Coupon;
import com.summit.stp.coupon.domain.repository.CouponRepository;
import com.summit.stp.coupon.infrastructure.persistence.mapper.CouponMapper;
import com.summit.stp.coupon.infrastructure.persistence.mapper.CouponUseScopeMapper;
import com.summit.stp.coupon.infrastructure.persistence.po.CouponPO;
import com.summit.stp.coupon.infrastructure.persistence.po.CouponUseScopePO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CouponRepositoryImpl implements CouponRepository {
    private final CouponMapper couponMapper;
    private final CouponUseScopeMapper couponUseScopeMapper;

    @Override
    public Coupon findCouponById(Long couponId) {
        CouponPO couponPO = couponMapper.selectById(couponId);
        if (couponPO == null) {
            return null;
        }

        // 查询关联的可用范围实体ID列表
        List<Long> relationIds = couponUseScopeMapper.selectList(
                new LambdaQueryWrapper<CouponUseScopePO>()
                        .eq(CouponUseScopePO::getCouponId, couponId)
        ).stream().map(CouponUseScopePO::getRelationId).collect(Collectors.toList());

        return Coupon.builder()
                .id(couponPO.getId())
                .name(couponPO.getName())
                .amount(couponPO.getAmount())
                .discount(couponPO.getDiscount())
                .status(couponPO.getStatus())
                .timeType(Coupon.CouponDateType.getByCode(couponPO.getTimeType()))
                .description(couponPO.getDescription())
                .validDays(couponPO.getValidDays())
                .validHours(couponPO.getValidHours())
                .scopeType(Coupon.CouponScopeType.fromCode(couponPO.getScopeType()))
                .scopeRelationIds(relationIds)
                .build();
    }

    @Override
    public void update(Coupon template) {
        CouponPO build = CouponPO.builder().build();
        BeanUtil.copyProperties(template, build);
        couponMapper.updateById(build);
    }
}
