package com.summit.stp.coupon.infrastructure.persistence;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.summit.stp.coupon.domain.model.Coupon;
import com.summit.stp.coupon.domain.repository.CouponRepository;
import com.summit.stp.coupon.infrastructure.persistence.mapper.CouponMapper;
import com.summit.stp.coupon.infrastructure.persistence.po.CouponPO;
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
        return Coupon.builder()
                .id(couponPO.getId())
                .name(couponPO.getName())
                .amount(couponPO.getAmount())
                .discount(couponPO.getDiscount())
                .status(couponPO.getStatus())
                .stock(couponPO.getStock())
                .build();
    }

    @Override
    public void update(Coupon template) {
        CouponPO build = CouponPO.builder().build();
        BeanUtil.copyProperties(template, build);
        couponMapper.updateById(build);
    }
}
