package com.summit.stp.admin.application.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.activity.application.service.CouponActivityCacheProvider;
import com.summit.stp.activity.domain.model.CouponActivity;
import com.summit.stp.activity.domain.repository.CouponActivityRepository;
import com.summit.stp.admin.application.command.CreateCouponActivityCommand;
import com.summit.stp.admin.application.command.CreateCouponCommand;
import com.summit.stp.admin.application.service.AdminCouponItemsService;
import com.summit.stp.admin.application.vo.AdminCouponActivityVO;
import com.summit.stp.admin.application.vo.AdminCouponVO;
import com.summit.stp.common.application.api.result.PageResult;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.common.util.DistributedLockUtil;
import com.summit.stp.coupon.domain.exception.NoSuchCouponException;
import com.summit.stp.coupon.domain.model.Coupon;
import com.summit.stp.coupon.domain.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.summit.stp.common.application.domain.exception.BusinessException;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminCouponItemsServiceImpl implements AdminCouponItemsService{

    /** 管理端写操作分布式锁等待时间（秒） */
    private static final long LOCK_WAIT_SECONDS = 3L;
    /** 管理端写操作分布式锁租约时间（秒） */
    private static final long LOCK_LEASE_SECONDS = 10L;

    private final CouponRepository couponRepository ;
    private final CouponActivityRepository couponActivityRepository;
    private final CouponActivityCacheProvider couponActivityCacheProvider;
    private final DistributedLockUtil distributedLockUtil;

    @Override
    public Result<PageResult<List<AdminCouponVO>>> list(String keyword, Integer status, Integer page, Integer pageSize) {
        Page<Coupon> p = couponRepository.list(keyword,status,page,pageSize);
        List<AdminCouponVO> list = p.getRecords().stream().map(this::buildAdminCouponVO).toList();
        return Result.success(new PageResult<>(p.getCurrent(), p.getTotal(), list));
    }

    @Override
    public Result<Void> create(CreateCouponCommand command) {
        boolean isUpdate = command.getId() != null;
        // 同一优惠券的新增/更新串行化（幂等/防重，#24）
        String lockKey = isUpdate ? "admin:coupon:save:" + command.getId() : "admin:coupon:save:new";
        return distributedLockUtil.executeWithLock(lockKey, LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, () -> {
            Timestamp now = Timestamp.from(Instant.now());
            Coupon coupon = Coupon.builder()
                    .id(command.getId())
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
                    // 更新分支不覆盖 createTime：置 null 由 MyBatis-Plus 默认 NOT_NULL 策略忽略该列，保留原创建时间（#33）
                    .createTime(isUpdate ? null : now)
                    .updateTime(now)
                    .build();
            if(isUpdate) couponRepository.updateById(coupon);else couponRepository.save(coupon);
            return Result.success();
        });
    }

    @Override
    public Result<Void> toggleBan(Long id, boolean attemptBan) {
        if (id == null) return Result.error("优惠券ID不能为空");
        return distributedLockUtil.executeWithLock("admin:coupon:ban:" + id, LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, () -> {
            Coupon coupon = couponRepository.findById(id).orElseThrow(NoSuchCouponException::new);
            if(attemptBan){
                coupon.ban();
            }else{
                coupon.active();
            }
            couponRepository.updateById(coupon);
            return Result.success();
        });
    }

    @Override
    public Result<Void> delete(Long id) {
        if (id == null) return Result.error("优惠券ID不能为空");
        return distributedLockUtil.executeWithLock("admin:coupon:delete:" + id, LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, () -> {
            couponRepository.deleteById(id);
            return Result.success();
        });
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
                .type(couponActivity.getType() != null ? couponActivity.getType().getCode() : 1)
                .couponId(couponActivity.getCouponId())
                .stock(couponActivity.getStock())
                .createTime(couponActivity.getCreateTime())
                .activityStartTime(couponActivity.getActivityStartTime())
                .activityEndTime(couponActivity.getActivityEndTime())
                .build();
    }

    @Override
    public void createActivity(CreateCouponActivityCommand command) {
        Long id = command.getId();
        validateCreateActivity(command);
        // 同一活动的保存串行化（幂等/防重，#24）
        String lockKey = id != null ? "admin:coupon:activity:save:" + id : "admin:coupon:activity:save:new";
        distributedLockUtil.executeWithLock(lockKey, LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, () -> {
            CouponActivity couponActivity = CouponActivity.builder()
                    .id(id)
                    .name(command.getName())
                    .status(command.getStatus())
                    .couponId(command.getCouponId())
                    .stock(command.getStock())
                    .type(CouponActivity.Type.fromCode(command.getType()))
                    .activityStartTime(command.getActivityStartTime())
                    .activityEndTime(command.getActivityEndTime())
                    .build();
            if(id != null){
                couponActivityRepository.update(couponActivity);
            }
            else couponActivityRepository.save(couponActivity);
        });
    }

    @Override
    public void toggleActivityStatus(Long id, boolean attemptStart) {
        if (id == null) throw new BusinessException("优惠券活动ID不能为空");
        distributedLockUtil.executeWithLock("admin:coupon:activity:status:" + id, LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, () -> {
            CouponActivity couponActivity = couponActivityRepository.findById(id).orElseThrow(() -> new BusinessException("优惠券活动不存在"));
            if(attemptStart){
                couponActivity.start();
                couponActivityCacheProvider.preWarmStock(couponActivity);
            }else {
                couponActivity.stop();
            }
            couponActivityRepository.update(couponActivity);
        });
    }

    @Override
    public void deleteActivity(Long id) {
        if (id == null) throw new BusinessException("优惠券活动ID不能为空");
        distributedLockUtil.executeWithLock("admin:coupon:activity:delete:" + id, LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, () -> {
            CouponActivity couponActivity = couponActivityRepository.findById(id)
                    .orElseThrow(() -> new BusinessException("优惠券活动不存在"));
            LocalDateTime now = LocalDateTime.now();
            if (!couponActivity.canDelete(now)) {
                throw new BusinessException("只能在活动结束之后或开始之前进行删除操作");
            }
            couponActivityRepository.delete(id);
        });
    }

    private AdminCouponVO buildAdminCouponVO(Coupon coupon) {
        return AdminCouponVO.builder()
                .id(coupon.getId())
                .name(coupon.getName())
                .discount(coupon.getDiscount())
                .amount(coupon.getAmount())
                .type(coupon.getType())
                .status(coupon.getStatus())
                // 枚举为空时返回 null 而非 NPE（#20）
                .scopeType(coupon.getScopeType() != null ? coupon.getScopeType().getCode() : null)
                .timeType(coupon.getTimeType() != null ? coupon.getTimeType().getCode() : null)
                .createTime(coupon.getCreateTime())
                .updateTime(coupon.getUpdateTime())
                .validDays(coupon.getValidDays())
                .validHours(coupon.getValidHours())
                .image(coupon.getImage())
                .description(coupon.getDescription())
                .build();
    }

    private void validateCreateActivity(CreateCouponActivityCommand command){
        Long couponId = command.getCouponId();
        String name = command.getName();
        Integer type = command.getType();
        LocalDateTime endTime = command.getActivityEndTime();
        LocalDateTime startTime = command.getActivityStartTime();
        if(couponId == null)throw new BusinessException("优惠券id不能为空");
        if(StrUtil.isBlank(name))throw new BusinessException("活动名称不能为空");
        if(type == null) throw new BusinessException("活动类型不能为空");
        if(endTime != null && startTime == null || endTime == null && startTime != null || startTime != null && startTime.isAfter(endTime))throw new BusinessException("活动时间异常");
    }
}
