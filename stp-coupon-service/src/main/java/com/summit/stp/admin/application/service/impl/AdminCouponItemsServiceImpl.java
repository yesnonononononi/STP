package com.summit.stp.admin.application.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.activity.domain.model.CouponActivity;
import com.summit.stp.activity.domain.repository.CouponActivityRepository;
import com.summit.stp.activity.infrastructure.persistence.CouponActivityRepositoryImpl;
import com.summit.stp.admin.application.command.CreateCouponActivityCommand;
import com.summit.stp.admin.application.command.CreateCouponCommand;
import com.summit.stp.admin.application.service.AdminCouponItemsService;
import com.summit.stp.admin.application.vo.AdminCouponActivityVO;
import com.summit.stp.admin.application.vo.AdminCouponVO;
import com.summit.stp.common.application.api.result.PageResult;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.coupon.domain.model.Coupon;
import com.summit.stp.coupon.domain.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminCouponItemsServiceImpl implements AdminCouponItemsService{
    private final CouponRepository couponRepository ;
    private final CouponActivityRepository couponActivityRepository;

    @Override
    public Result<PageResult<List<AdminCouponVO>>> list(String keyword, Integer status, Integer page, Integer pageSize) {
        Page<Coupon> p = couponRepository.list(keyword,status,page,pageSize);
        List<AdminCouponVO> list = p.getRecords().stream().map(this::buildAdminCouponVO).toList();
        return Result.success(new PageResult<>(p.getCurrent(), p.getTotal(), list));
    }

    @Override
    public Result<Void> create(CreateCouponCommand command) {
        Coupon coupon = Coupon.builder()
                .name(command.getName())
                .discount(command.getDiscount())
                .amount(command.getAmount())
                .status(command.getStatus())
                .type(command.getType())
                .description(command.getDescription())
                .image(command.getImage())
                .scopeType(command.getScopeType())
                .timeType(command.getTimeType())
                .validDays(command.getValidDays())
                .validHours(command.getValidHours())
                .createTime(Timestamp.from(Instant.now()))
                .updateTime(Timestamp.from(Instant.now()))
                .build();
        couponRepository.save(coupon);
        return Result.success();
    }

    @Override
    public Result<Void> toggleBan(Long id, boolean attemptBan) {
        Coupon coupon = couponRepository.findCouponById(id);
        if(attemptBan){
            coupon.ban();
        }else{
            coupon.active();
        }
        couponRepository.updateById(coupon);
        return Result.success();
    }

    @Override
    public Result<Void> delete(Long id) {
        couponRepository.deleteById(id);
        return Result.success();
    }

    @Override
    public Result<PageResult<List<AdminCouponActivityVO>>> listActivity(String keyword, Integer status, Integer page, Integer pageSize) {
        Page<CouponActivity> p = couponActivityRepository.list(keyword,status,page,pageSize);
        List<AdminCouponActivityVO> list = p.getRecords().stream().map(this::buildAdminCouponActivityVO).toList();
        return Result.success(new PageResult<>(p.getCurrent(), p.getTotal(), list));
    }

    private AdminCouponActivityVO buildAdminCouponActivityVO(CouponActivity couponActivity) {
        return AdminCouponActivityVO.builder()
                .id(couponActivity.getId())
                .name(couponActivity.getName())
                .status(couponActivity.getStatus())
                .stock(couponActivity.getStock())
                .activityStartTime(couponActivity.getActivityStartTime())
                .activityEndTime(couponActivity.getActivityEndTime())
                .build();
    }

    @Override
    public void createActivity(CreateCouponActivityCommand command) {
        CouponActivity couponActivity = CouponActivity.builder()
                .name(command.getName())
                .status(command.getStatus())
                .limitQuantity(command.getLimitQuantity())
                .couponId(command.getCouponId())
                .stock(command.getStock())
                .type(CouponActivity.Type.fromCode(command.getType()))
                .activityStartTime(command.getActivityStartTime())
                .activityEndTime(command.getActivityEndTime())
                .build();
        couponActivityRepository.save(couponActivity);
    }

    @Override
    public void toggleActivityStatus(Long id, boolean attemptStart) {
        CouponActivity couponActivity = couponActivityRepository.findById(id).orElseThrow(() -> new RuntimeException("优惠券活动不存在"));
        if(attemptStart){
            couponActivity.start();
        }else{
            couponActivity.stop();
        }
        couponActivityRepository.update(couponActivity);
    }

    private AdminCouponVO buildAdminCouponVO(Coupon coupon) {
        return AdminCouponVO.builder()
                .id(coupon.getId())
                .name(coupon.getName())
                .discount(coupon.getDiscount())
                .amount(coupon.getAmount())
                .type(coupon.getType())
                .status(coupon.getStatus())
                .scopeType(coupon.getScopeType().getCode())
                .timeType(coupon.getTimeType().getCode())
                .createTime(coupon.getCreateTime())
                .updateTime(coupon.getUpdateTime())
                .validDays(coupon.getValidDays())
                .validHours(coupon.getValidHours())
                .image(coupon.getImage())
                .description(coupon.getDescription())
                .build();
    }
}
