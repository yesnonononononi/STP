package com.summit.stp.activity.application.service.impl;

import cn.hutool.core.util.IdUtil;
import com.summit.stp.activity.application.dto.CouponActivityDTO;
import com.summit.stp.activity.application.service.CouponActivityAppService;
import com.summit.stp.activity.application.service.CouponActivityCacheProvider;
import com.summit.stp.activity.application.vo.CouponActivityQueryVO;
import com.summit.stp.activity.domain.model.CouponActivity;
import com.summit.stp.activity.domain.repository.CouponActivityRepository;
import com.summit.stp.common.auth.UserHolder;
import com.summit.stp.user.api.vo.MemberTypeVO;
import com.summit.stp.user.api.vo.MemberVO;
import com.summit.stp.common.application.domain.exception.BusinessException;
import com.summit.stp.common.application.domain.exception.UnPermissionException;
import com.summit.stp.user.api.client.MemberFeignClient;
import com.summit.stp.coupon.domain.exception.NoSuchCouponException;
import com.summit.stp.coupon.domain.model.Coupon;
import com.summit.stp.coupon.domain.model.CouponUseScope;
import com.summit.stp.coupon.domain.repository.CouponRepository;
import com.summit.stp.coupon.domain.repository.CouponUseScopeRepository;
import com.summit.stp.user_coupon.domain.model.CouponStatus;
import com.summit.stp.user_coupon.domain.model.UserCoupon;
import com.summit.stp.user_coupon.domain.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponActivityAppServiceImpl implements CouponActivityAppService {
    private final CouponActivityRepository couponActivityRepository;
    private final CouponActivityCacheProvider couponActivityCacheProvider;
    private final CouponRepository couponRepository;
    private final CouponUseScopeRepository couponUseScopeRepository;
    private final UserCouponRepository userCouponRepository;
    private final MemberFeignClient memberFeignClient;
    private final TransactionTemplate transactionTemplate;

    @Override
    public List<CouponActivityQueryVO> queryAllActivitiesByScopeType(Integer scopeType) {
        Coupon.CouponScopeType requestedScope = Coupon.CouponScopeType.fromCode(scopeType);
        if (requestedScope == null) {
            throw new BusinessException("无效的活动类型");
        }

        LocalDateTime now = LocalDateTime.now();
        List<CouponActivity> activities = couponActivityRepository.findByScopeType(scopeType).stream()
                .filter(activity -> activity.isAvailable(now))
                .toList();
        if (activities.isEmpty()) {
            return List.of();
        }

        List<Long> couponIds = activities.stream()
                .map(CouponActivity::getCouponId)
                .distinct()
                .toList();
        Map<Long, Coupon> templates = couponRepository.findByIds(couponIds).stream()
                .collect(Collectors.toMap(Coupon::getId, coupon -> coupon));
        Map<Long, CouponUseScope> scopes = couponUseScopeRepository.findByCouponIds(couponIds);

        Long currentUserId = UserHolder.getUser() != null ? UserHolder.getUser().getId() : null;
        long maxDuration = activities.stream()
                .mapToLong(a -> a.getDuration(now))
                .max()
                .orElse(0L);
        Map<Long, Integer> receivedCounts = batchCheckUserReceivedCounts(currentUserId, couponIds, maxDuration);

        List<Long> relationIds = scopes.values().stream()
                .map(CouponUseScope::getRelationId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, MemberTypeVO> memberTypes = requestedScope == Coupon.CouponScopeType.SCOPE_OF_PRODUCT_TYPE
                ? queryMemberTypes(relationIds)
                : Map.of();
        Map<Long, MemberVO> members = requestedScope == Coupon.CouponScopeType.SCOPE_OF_PRODUCT
                ? queryMembers(relationIds)
                : Map.of();

        return activities.stream()
                .map(activity -> toActivityVO(activity, templates, scopes, receivedCounts, memberTypes, members))
                .toList();
    }

    @Override
    public Map<Long, Integer> batchCheckUserReceivedCounts(Long userId, List<Long> couponIds, Long maxDurationMs) {
        if (userId == null || couponIds == null || couponIds.isEmpty()) {
            return Map.of();
        }
        return couponActivityCacheProvider.batchGetUserReceivedCounts(userId, couponIds, maxDurationMs);
    }

    @Override
    public void receiveActivityCoupon(Long activityId) {
        Long currentUserId = UserHolder.getUser().getId();

        CouponActivity activity = couponActivityRepository.findById(activityId)
                .orElseThrow(() -> new BusinessException("该优惠券活动不存在！"));
        LocalDateTime now = LocalDateTime.now();
        if (!activity.isAvailable(now)) {
            throw new BusinessException("当前活动未开始或者已经结束");
        }
        Coupon coupon = couponRepository.findCouponById(activity.getCouponId());
        if (coupon == null) {
            throw new BusinessException("优惠券不存在！");
        }
        Long couponId = coupon.getId();

        LocalDateTime endTime = getEndTime(now, coupon, activity);

        UserCoupon userCoupon = UserCoupon.builder()
                .id(null)
                .userId(currentUserId)
                .couponTemplateId(couponId)
                .status(CouponStatus.NOT_USE)
                .createTime(new Timestamp(System.currentTimeMillis()))
                .endTime(Timestamp.valueOf(endTime))
                .build();

        long limitQuantity = activity.getLimitQuantity() != null ? activity.getLimitQuantity() : Long.MAX_VALUE;
        couponActivityCacheProvider.cacheUserLimit(currentUserId, couponId, activity.getDuration(now));
        boolean isOk = couponActivityCacheProvider.deductStock(couponId, limitQuantity, activityId);
        if (!isOk) {
            throw new BusinessException("优惠券库存不足！");
        }
        transactionTemplate.executeWithoutResult(status -> {
            try {
                userCouponRepository.save(userCoupon);
                log.info("【优惠券活动】领取成功 优惠券ID:{} 用户ID:{}", couponId, currentUserId);
            } catch (Exception e) {
                couponActivityCacheProvider.increaseStock(couponId, currentUserId, activityId);
                log.error("【优惠券活动】领取失败 优惠券ID:{} 用户ID:{}", couponId, currentUserId, e);
                throw e;
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveActivity(CouponActivityDTO dto) {
        if (UserHolder.getUser().getAdmin() == 0) {
            throw new UnPermissionException("无权限操作");
        }
        Long couponId = dto.getCouponId();
        CouponActivity activity = CouponActivity.builder()
                .id(IdUtil.getSnowflakeNextId())
                .couponId(couponId)
                .name(dto.getName())
                .type(CouponActivity.Type.fromCode(dto.getType()))
                .stock(dto.getStock())
                .activityStartTime(dto.getActivityStartTime())
                .activityEndTime(dto.getActivityEndTime())
                .status(dto.getStatus())
                .limitQuantity(dto.getLimitQuantity())
                .build();
        couponActivityRepository.save(activity);
        couponActivityCacheProvider.preWarmStock(activity);
    }

    @Override
    public void updateActivity(CouponActivityDTO dto) {
        CouponActivity activity = CouponActivity.builder()
                .id(dto.getId())
                .couponId(dto.getCouponId())
                .name(dto.getName())
                .stock(dto.getStock())
                .type(CouponActivity.Type.fromCode(dto.getType()))
                .activityStartTime(dto.getActivityStartTime())
                .activityEndTime(dto.getActivityEndTime())
                .status(dto.getStatus())
                .limitQuantity(dto.getLimitQuantity())
                .build();
        couponActivityRepository.update(activity);
    }

    @Override
    public void deleteActivity(Long id) {
        couponActivityRepository.delete(id);
    }

    private Map<Long, MemberTypeVO> queryMemberTypes(List<Long> ids) {
        return ids.isEmpty() ? Map.of() : emptyIfNull(memberFeignClient.queryMemberTypeByIds(ids).getData());
    }

    private Map<Long, MemberVO> queryMembers(List<Long> ids) {
        return ids.isEmpty() ? Map.of() : emptyIfNull(memberFeignClient.queryMemberByIds(ids).getData());
    }

    private CouponActivityQueryVO toActivityVO(CouponActivity activity,
                                                Map<Long, Coupon> templates,
                                                Map<Long, CouponUseScope> scopes,
                                                Map<Long, Integer> receivedCounts,
                                                Map<Long, MemberTypeVO> memberTypes,
                                                Map<Long, MemberVO> members) {
        Coupon template = templates.get(activity.getCouponId());
        if (template == null) {
            throw new NoSuchCouponException("未找到优惠券信息");
        }

        CouponUseScope scope = scopes.get(activity.getCouponId());
        Integer limitQuantity = activity.getLimitQuantity();
        int receivedCount = receivedCounts.getOrDefault(activity.getCouponId(), 0);
        LocalDateTime now = LocalDateTime.now();
        boolean hasQualification = (limitQuantity == null || receivedCount < limitQuantity);
        boolean hasStock = (activity.getStock() == null || activity.getStock() > 0);
        boolean inActivityTime = activity.isAvailable(now);

        return CouponActivityQueryVO.builder()
                .id(activity.getId())
                .couponId(activity.getCouponId())
                .name(activity.getName())
                .stock(activity.getStock())
                .activityStartTime(activity.getActivityStartTime())
                .activityEndTime(activity.getActivityEndTime())
                .status(activity.getStatus())
                .couponName(template.getName())
                .discount(template.getDiscount())
                .validDays(template.getValidDays())
                .validHours(template.getValidHours())
                .scopeDescription(resolveScopeDescription(template, scope, memberTypes, members))
                .limitQuantity(limitQuantity)
                .amount(template.getAmount())
                .timeType(template.getTimeType() == null ? null : template.getTimeType().getCode())
                .isAvailable(hasQualification && hasStock && inActivityTime)
                .couponType(template.getAmount() != null ? 1 : 0)
                .scopeType(template.getScopeType() == null
                        ? Coupon.CouponScopeType.ALL_SCOPE.getCode()
                        : template.getScopeType().getCode())
                .description(template.getDescription())
                .type(activity.getType() == null ? null : activity.getType().getCode())
                .build();
    }

    private String resolveScopeDescription(Coupon template,
                                            CouponUseScope scope,
                                            Map<Long, MemberTypeVO> memberTypes,
                                            Map<Long, MemberVO> members) {
        if (scope == null || scope.getRelationId() == null || template.getScopeType() == null) {
            return "";
        }
        return switch (template.getScopeType()) {
            case SCOPE_OF_PRODUCT_TYPE -> {
                MemberTypeVO memberType = memberTypes.get(scope.getRelationId());
                yield memberType == null ? "" : memberType.getName();
            }
            case SCOPE_OF_PRODUCT -> {
                MemberVO member = members.get(scope.getRelationId());
                yield member == null ? "" : member.getName();
            }
            default -> "";
        };
    }

    private LocalDateTime getEndTime(LocalDateTime now, Coupon coupon, CouponActivity activity) {
        LocalDateTime endTime = now;
        if (coupon.getTimeType() == Coupon.CouponDateType.FIXED_TIME_PERIOD) {
            endTime = activity.getActivityEndTime();
        } else if (coupon.getTimeType() == Coupon.CouponDateType.RECEIVE_EFFECT_TIME_PERIOD_BY_DAY) {
            endTime = now.plusDays(coupon.getValidDays());
        } else if (coupon.getTimeType() == Coupon.CouponDateType.RECEIVE_EFFECT_TIME_PERIOD_BY_HOUR) {
            endTime = now.plusHours(coupon.getValidHours());
        }
        return endTime;
    }

    private <K, V> Map<K, V> emptyIfNull(Map<K, V> values) {
        return values == null ? Map.of() : values;
    }
}

