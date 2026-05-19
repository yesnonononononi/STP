package com.summit.stp.payment.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.payment.domain.model.Coupon;
import com.summit.stp.payment.domain.model.UserCoupon;
import com.summit.stp.payment.domain.repository.CouponRepository;
import com.summit.stp.payment.domain.repository.UserCouponRepository;
import com.summit.stp.payment.infrastructure.persistence.mapper.UserCouponMapper;
import com.summit.stp.payment.infrastructure.persistence.po.UserCouponPO;
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
        UserCoupon userCoupon = UserCoupon.of(
                po.getId(),
                po.getUserId(),
                po.getCouponId(),
                po.getStatus(),
                po.getCreateTime(),
                po.getUpdateTime()
        );
        // 加载关联的优惠券模板并进行绑定
        Coupon template = couponRepository.findCouponById(po.getCouponId());
        if (template != null) {
            userCoupon.bindTemplate(template);
        }
        return userCoupon;
    }

    @Override
    public void save(UserCoupon userCoupon) {
        UserCouponPO po = new UserCouponPO();
        po.setId(userCoupon.getId());
        po.setUserId(userCoupon.getUserId());
        po.setCouponId(userCoupon.getCouponTemplateId());
        po.setStatus(userCoupon.getStatus().getCode());
        po.setCreateTime(userCoupon.getCreateTime());
        po.setUpdateTime(userCoupon.getUpdateTime());

        if (po.getId() == null || userCouponMapper.selectById(po.getId()) == null) {
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
                .map(po -> {
                    UserCoupon userCoupon = UserCoupon.of(
                            po.getId(),
                            po.getUserId(),
                            po.getCouponId(),
                            po.getStatus(),
                            po.getCreateTime(),
                            po.getUpdateTime()
                    );
                    Coupon template = couponRepository.findCouponById(po.getCouponId());
                    if (template != null) {
                        userCoupon.bindTemplate(template);
                    }
                    return userCoupon;
                })
                .collect(Collectors.toList());

        domainPage.setRecords(domainList);
        return domainPage;
    }
}
