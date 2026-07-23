package com.summit.stp.coupon.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.coupon.domain.model.Coupon;
import com.summit.stp.coupon.domain.model.CouponStatus;
import com.summit.stp.coupon.domain.model.UserCoupon;
import com.summit.stp.coupon.domain.repository.CouponRepository;
import com.summit.stp.coupon.domain.repository.UserCouponRepository;
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
        po.setUsedTime(userCoupon.getUsedTime());
        po.setCreateTime(userCoupon.getCreateTime());
        po.setUpdateTime(userCoupon.getUpdateTime());
        po.setEndTime(userCoupon.getEndTime());
        po.setOrderId(userCoupon.getOrderId());

        if (po.getId() == null) {
            userCouponMapper.insert(po);
        } else {
            userCouponMapper.updateById(po);
        }
    }

    @Override
    public Page<UserCoupon> queryHistoryByUser(Long userId, long page, long pageSize,CouponStatus status) {
        Page<UserCouponPO> poPage = userCouponMapper.selectPage(
                new Page<>(page, pageSize),
                status == null ? new LambdaQueryWrapper<UserCouponPO>()
                        .eq(UserCouponPO::getUserId, userId)
                        : new LambdaQueryWrapper<UserCouponPO>()
                        .eq(UserCouponPO::getUserId, userId)
                        .eq(UserCouponPO::getStatus, status.getCode())
        );

        Page<UserCoupon> domainPage = new Page<>(poPage.getCurrent(), poPage.getSize(), poPage.getTotal());
        
        List<UserCoupon> domainList = poPage.getRecords().stream()
                .map(this::getUserCoupon)
                .collect(Collectors.toList());

        domainPage.setRecords(domainList);
        return domainPage;
    }

    @Override
    public List<UserCoupon> findUnusedByUserId(Long userId) {
        List<UserCouponPO> pos = userCouponMapper.selectList(
                new LambdaQueryWrapper<UserCouponPO>()
                        .eq(UserCouponPO::getUserId, userId)
                        .eq(UserCouponPO::getStatus, CouponStatus.NOT_USE.getCode())
                        .orderByDesc(UserCouponPO::getCreateTime)
        );
        return pos.stream()
                .map(this::getUserCoupon)
                .collect(Collectors.toList());
    }

    @Override
    public Integer countUnUsedByCouponId(Long couponId) {
        return Math.toIntExact(userCouponMapper.selectCount(new LambdaQueryWrapper<UserCouponPO>().eq(UserCouponPO::getCouponId, couponId).eq(UserCouponPO::getStatus, CouponStatus.NOT_USE.getCode())));
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
                .usedTime(po.getUsedTime())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .orderId(po.getOrderId())
                .endTime(po.getEndTime())
                .build();
        Coupon template = couponRepository.findCouponById(po.getCouponId());
        if (template != null) {
            userCoupon.bindTemplate(template);
        }
        return userCoupon;
    }
}
