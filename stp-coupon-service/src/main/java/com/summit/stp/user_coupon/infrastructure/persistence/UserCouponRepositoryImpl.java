package com.summit.stp.user_coupon.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.devframeworkdddstarter.repo.AbstractRepository;
import com.summit.stp.common.application.domain.exception.BusinessException;
import com.summit.stp.coupon.domain.model.Coupon;
import com.summit.stp.coupon.domain.repository.CouponRepository;
import com.summit.stp.user_coupon.domain.model.CouponStatus;
import com.summit.stp.user_coupon.domain.model.UserCoupon;
import com.summit.stp.user_coupon.domain.repository.UserCouponRepository;
import com.summit.stp.user_coupon.infrastructure.persistence.mapper.UserCouponMapper;
import com.summit.stp.user_coupon.infrastructure.persistence.po.UserCouponPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Repository
public class UserCouponRepositoryImpl extends AbstractRepository<UserCoupon, UserCouponPO> implements UserCouponRepository {
    @Autowired
    private  CouponRepository couponRepository;


    public UserCouponRepositoryImpl(BaseMapper<UserCouponPO> baseMapper) {
        super(baseMapper);
    }


    @Override
    public UserCoupon findUserCouponById(Long id) {
        return findBy(id, UserCouponPO::getId).orElse(null);
    }

    @Override
    public void save(UserCoupon userCoupon) {
        if (userCoupon == null) return;
        if (userCoupon.getId() != null && findById(userCoupon.getId()).isPresent()) {
            super.updateById(userCoupon);
        } else {
            super.save(userCoupon);
        }
    }

    @Override
    public Page<UserCoupon> queryHistoryByUser(Long userId, long page, long pageSize, CouponStatus status) {
        Page<UserCouponPO> poPage = getBaseMapper().selectPage(
                new Page<>(page, pageSize),
                status == null ? new LambdaQueryWrapper<UserCouponPO>()
                        .eq(UserCouponPO::getUserId, userId)
                        : new LambdaQueryWrapper<UserCouponPO>()
                        .eq(UserCouponPO::getUserId, userId)
                        .eq(UserCouponPO::getStatus, status.getCode())
        );

        Page<UserCoupon> domainPage = new Page<>(poPage.getCurrent(), poPage.getSize(), poPage.getTotal());
        List<UserCoupon> domainList = poPage.getRecords().stream()
                .map(this::toModel)
                .collect(Collectors.toList());

        domainPage.setRecords(domainList);
        return domainPage;
    }

    @Override
    public List<UserCoupon> findUnusedByUserId(Long userId) {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        List<UserCouponPO> pos = getBaseMapper().selectList(
                new LambdaQueryWrapper<UserCouponPO>()
                        .eq(UserCouponPO::getUserId, userId)
                        .eq(UserCouponPO::getStatus, CouponStatus.NOT_USE.getCode())
                        .isNull(UserCouponPO::getUsedTime)
                        .gt(UserCouponPO::getEndTime, now)
                        .orderByDesc(UserCouponPO::getCreateTime)
        );
        return pos.stream()
                .map(this::toModel)
                .filter(UserCoupon::isAvailable)
                .collect(Collectors.toList());
    }



    @Override
    public Map<Long, Integer> countByUserIdAndCouponIds(Long userId, Collection<Long> couponIds) {
        if (userId == null || couponIds == null || couponIds.isEmpty()) {
            return Map.of();
        }
        return getBaseMapper().selectList(new LambdaQueryWrapper<UserCouponPO>()
                        .select(UserCouponPO::getCouponId)
                        .eq(UserCouponPO::getUserId, userId)
                        .in(UserCouponPO::getCouponId, couponIds))
                .stream()
                .collect(Collectors.groupingBy(UserCouponPO::getCouponId, Collectors.summingInt(ignored -> 1)));
    }



    @Override
    protected UserCouponPO toPO(UserCoupon userCoupon) {
        if (userCoupon == null) return null;
        UserCouponPO po = new UserCouponPO();
        po.setId(userCoupon.getId());
        po.setUserId(userCoupon.getUserId());
        po.setCouponId(userCoupon.getCouponTemplateId());
        po.setStatus(userCoupon.getStatus() != null ? userCoupon.getStatus().getCode() : CouponStatus.NOT_USE.getCode());
        po.setUsedTime(userCoupon.getUsedTime());
        po.setCreateTime(userCoupon.getCreateTime());
        po.setUpdateTime(userCoupon.getUpdateTime());
        po.setEndTime(userCoupon.getEndTime());
        po.setOrderId(userCoupon.getOrderId());
        return po;
    }

    @Override
    protected UserCoupon toModel(UserCouponPO po) {
        if (po == null) return null;
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
        if (po.getCouponId() != null) {
            couponRepository.findById(po.getCouponId()).ifPresent(userCoupon::bindTemplate);
        }
        return userCoupon;
    }
}

