package com.summit.stp.activity.application.service.impl;

import com.summit.stp.activity.application.service.CouponActivityAppService;
import com.summit.stp.activity.application.service.CouponActivityCacheProvider;
import com.summit.stp.activity.application.vo.CouponActivityQueryVO;
import com.summit.stp.activity.domain.event.SeckillEvent;
import com.summit.stp.activity.domain.model.CouponActivity;
import com.summit.stp.activity.domain.repository.CouponActivityRepository;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.common.auth.UserHolder;
import com.summit.stp.user.api.vo.MemberTypeVO;
import com.summit.stp.user.api.vo.MemberVO;
import com.summit.stp.common.application.domain.exception.BusinessException;
import com.summit.stp.user.api.client.MemberFeignClient;
import com.summit.stp.coupon.domain.exception.NoSuchCouponException;
import com.summit.stp.coupon.domain.model.Coupon;
import com.summit.stp.coupon.domain.model.CouponUseScope;
import com.summit.stp.coupon.domain.repository.CouponRepository;
import com.summit.stp.coupon.domain.repository.CouponUseScopeRepository;
import com.summit.stp.user_coupon.domain.model.CouponStatus;
import com.summit.stp.user_coupon.domain.model.UserCoupon;
import com.summit.stp.user_coupon.infrastructure.persistence.UserCouponRepositoryImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
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
    private final MemberFeignClient memberFeignClient;
    private final CouponQueueSender couponQueueSender;
    private final UserCouponRepositoryImpl userCouponRepositoryImpl;

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

        Long currentUserId = UserHolder.getUser().getId();
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
                .map(activity -> assembleVO(activity, templates, scopes, receivedCounts, memberTypes, members))
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
    public Result<Void> receiveActivityCoupon(Long activityId) {
        Long currentUserId = UserHolder.getUser().getId();
        LocalDateTime now = LocalDateTime.now();

        //1, 查询优惠券营销活动并校验
        CouponActivity activity = couponActivityRepository.findById(activityId).orElseThrow(() -> new BusinessException("该优惠券活动不存在！"));

        if (!activity.isAvailable(now)) {
            throw new BusinessException("当前活动未开始或者已经结束");
        }
        Coupon coupon = couponRepository.findById(activity.getCouponId()).orElseThrow(NoSuchCouponException::new);

        Long couponId = coupon.getId();

        //2, 确保缓存存在用户领取记录
        couponActivityCacheProvider.cacheUserLimit(currentUserId, couponId, activity.getDuration(now));
        UserCoupon userCoupon = UserCoupon.builder()
                .userId(currentUserId)
                .couponTemplateId(couponId)
                .endTime(getEndTime(now, coupon, activity))
                .createTime(Timestamp.from(Instant.now()))
                .orderId(null)
                .status(CouponStatus.NOT_USE)
                .template(coupon)
                .updateTime(Timestamp.from(Instant.now()))
                .build();
        try {
            //3, 原子秒杀优惠券(减库存, 且一人只可领 1 张)
            if (!couponActivityCacheProvider.deductStock(couponId, activityId)) {
                return Result.error("优惠券库存不足");
            }
        } catch (Exception e) {
            log.error("【优惠券秒杀】缓存服务暂不可用", e);
            return Result.error("优惠券秒杀暂不可用,请稍后再试");
        }
        try {
            //4, 优惠券异步落库
            couponQueueSender.send(SeckillEvent.builder().userCoupon(
                    userCoupon
            ).build());
        } catch (Exception e) {
            log.error("【优惠券秒杀-消息发送】mq消息发送失败,即将回滚", e);
            return Result.error("优惠券领取失败,请过一会重试");
        }
        return Result.success();
    }


    private Map<Long, MemberTypeVO> queryMemberTypes(List<Long> ids) {
        return ids.isEmpty() ? Map.of() : emptyIfNull(memberFeignClient.queryMemberTypeByIds(ids).getData());
    }

    private Map<Long, MemberVO> queryMembers(List<Long> ids) {
        return ids.isEmpty() ? Map.of() : emptyIfNull(memberFeignClient.queryMemberByIds(ids).getData());
    }

    private CouponActivityQueryVO assembleVO(CouponActivity activity,
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
        int receivedCount = receivedCounts.getOrDefault(activity.getCouponId(), 0);
        LocalDateTime now = LocalDateTime.now();
        boolean hasQualification = (receivedCount < 1);
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
                .amount(template.getAmount())
                .timeType(template.getTimeType() == null ? null : template.getTimeType().getCode())
                .isAvailable(hasQualification && hasStock && inActivityTime)
                .couponType(template.getAmount() != null ? 1 : 0)
                .scopeType(template.getScopeType() == null
                        ? Coupon.CouponScopeType.ALL_SCOPE.getCode()
                        : template.getScopeType().getCode())
                .description(template.getDescription())
                .image(template.getImage())
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

    private Timestamp getEndTime(LocalDateTime now, Coupon coupon, CouponActivity activity) {
        LocalDateTime endTime = now;
        if (coupon.getTimeType() == Coupon.CouponDateType.FIXED_TIME_PERIOD) {
            endTime = activity.getActivityEndTime();
        } else if (coupon.getTimeType() == Coupon.CouponDateType.RECEIVE_EFFECT_TIME_PERIOD_BY_DAY) {
            endTime = now.plusDays(coupon.getValidDays());
        } else if (coupon.getTimeType() == Coupon.CouponDateType.RECEIVE_EFFECT_TIME_PERIOD_BY_HOUR) {
            endTime = now.plusHours(coupon.getValidHours());
        }
        return Timestamp.valueOf(endTime);
    }

    private <K, V> Map<K, V> emptyIfNull(Map<K, V> values) {
        return values == null ? Map.of() : values;
    }


}

