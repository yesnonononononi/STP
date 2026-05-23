package com.summit.stp.coupon.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.coupon.domain.model.Coupon;
import com.summit.stp.coupon.domain.model.UserCoupon;
import com.summit.stp.coupon.domain.repository.CouponRepository;
import com.summit.stp.coupon.domain.repository.UserCouponRepository;
import com.summit.stp.coupon.domain.model.CouponStatus;
import com.summit.stp.coupon.infrastructure.persistence.mapper.UserCouponMapper;
import com.summit.stp.coupon.infrastructure.persistence.po.UserCouponPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class UserCouponRepositoryImpl implements UserCouponRepository {
    private final UserCouponMapper userCouponMapper;
    private final CouponRepository couponRepository;

    @Override
    public UserCoupon findUserCouponById(Long id) {
        UserCouponPO po = userCouponMapper.selectById(id);
        if (po == null) {
            return null;
        }
        return getUserCoupon(po);
    }

    @Override
    public void save(UserCoupon userCoupon) {
        UserCouponPO po = new UserCouponPO();
        po.setId(userCoupon.getId());
        po.setUserId(userCoupon.getUserId());
        po.setCouponId(userCoupon.getCouponTemplateId());
        po.setStatus(userCoupon.getStatus().getCode());
        po.setOrderId(userCoupon.getOrderId());
        po.setUsedTime(userCoupon.getUsedTime());
        po.setCreateTime(userCoupon.getCreateTime());
        po.setUpdateTime(userCoupon.getUpdateTime());

        if (po.getId() == null) {
            userCouponMapper.insert(po);
        } else {
            userCouponMapper.updateById(po);
        }
    }

    @Override
    public Page<UserCoupon> queryHistoryByUser(Long userId, long page, long pageSize) {
        Page<UserCouponPO> poPage = userCouponMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<UserCouponPO>()
                        .eq(UserCouponPO::getUserId, userId)
                        .orderByDesc(UserCouponPO::getCreateTime)
        );

        Page<UserCoupon> domainPage = new Page<>(poPage.getCurrent(), poPage.getSize(), poPage.getTotal());
        
        List<UserCoupon> domainList = poPage.getRecords().stream()
                .map(this::getUserCoupon)
                .collect(Collectors.toList());

        domainPage.setRecords(domainList);
        return domainPage;
    }


    /**
     * 获取领域模型对象
     * @param po
     * @return
     */
    private UserCoupon getUserCoupon(UserCouponPO po) {
        UserCoupon userCoupon = UserCoupon.builder()
                .id(po.getId())
                .userId(po.getUserId())
                .couponTemplateId(po.getCouponId())
                .status(CouponStatus.fromCode(po.getStatus()))
                .orderId(po.getOrderId())
                .usedTime(po.getUsedTime())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
        Coupon template = couponRepository.findCouponById(po.getCouponId());
        if (template != null) {
            userCoupon.bindTemplate(template);
        }
        return userCoupon;
    }
}
