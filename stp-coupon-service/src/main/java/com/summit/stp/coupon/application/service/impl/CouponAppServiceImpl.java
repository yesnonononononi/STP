package com.summit.stp.coupon.application.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.common.ThreadContext.UserHolder;
import com.summit.stp.common.application.domain.exception.BusinessException;
import com.summit.stp.common.application.domain.exception.ParameterException;
import com.summit.stp.common.application.domain.exception.UnPermissionException;
import com.summit.stp.common.application.api.vo.CouponQueryVO;
import com.summit.stp.common.application.api.vo.MemberTypeVO;
import com.summit.stp.common.application.api.vo.MemberVO;
import com.summit.stp.common.application.api.vo.OrderQueryVO;
import com.summit.stp.common.feign.MemberFeignClient;
import com.summit.stp.common.feign.OrderFeignClient;
import com.summit.stp.common.util.DistributedLockUtil;
import com.summit.stp.coupon.application.dto.CouponActivityDTO;
import com.summit.stp.coupon.application.service.CouponAppService;
import com.summit.stp.coupon.application.vo.CouponActivityQueryVO;
import com.summit.stp.coupon.domain.exception.NoSuchCouponException;
import com.summit.stp.coupon.domain.model.*;
import com.summit.stp.coupon.domain.repository.CouponActivityRepository;
import com.summit.stp.coupon.domain.repository.CouponUseScopeRepository;
import com.summit.stp.coupon.domain.repository.CouponRepository;
import com.summit.stp.coupon.domain.repository.UserCouponRepository;
import com.summit.stp.coupon.domain.service.CouponCacheProvider;
import com.summit.stp.coupon.infrastructure.constants.CouponConstants;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponAppServiceImpl implements CouponAppService {
    private final UserCouponRepository userCouponRepository;
    private final CouponRepository couponRepository;
    private final CouponActivityRepository couponActivityRepository;
    private final OrderFeignClient orderFeignClient;
    private final MemberFeignClient memberFeignClient;
    private final DistributedLockUtil distributedLockUtil;
    private final TransactionTemplate transactionTemplate;
    private final CouponCacheProvider couponCacheProvider;
    private final CouponUseScopeRepository couponUseScopeRepository;


    @Override
    public List<CouponQueryVO> queryAvailableCoupons() {
        Long currentUserId = UserHolder.getUser().getId();
        List<UserCoupon> unused = userCouponRepository.findUnusedByUserId(currentUserId);
        return unused.stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    @Override
    public CouponQueryVO queryById(Long id) {
        UserCoupon userCoupon = userCouponRepository.findUserCouponById(id);
        if (userCoupon == null) {
            return null;
        }
        return convert(userCoupon);
    }

    @Override
    public Page<CouponQueryVO> queryHistory(long page, long pageSize, CouponStatus status) {
        Long currentUserId = UserHolder.getUser().getId();
        Page<UserCoupon> userCouponPage = userCouponRepository.queryHistoryByUser(currentUserId, page, pageSize, status);
        List<UserCoupon> records = userCouponPage.getRecords();
        List<Long> ids = records.stream().map(UserCoupon::getId).toList();

        Page<CouponQueryVO> voPage = new Page<>(userCouponPage.getCurrent(), userCouponPage.getSize(), userCouponPage.getTotal());

        //查询优惠券被订单使用信息
        Map<Long, OrderQueryVO> packageIdMap = orderFeignClient.findOrderByCouponIds(currentUserId, ids).getData(); //优惠券id -> order
        List<Long> packageIds = packageIdMap.values().stream().map(OrderQueryVO::getMemberId).toList();
        Map<Long, MemberVO> memberMap = memberFeignClient.queryMemberByIds(packageIds).getData(); // 商品套餐id -> 商品名称

        List<CouponQueryVO> voList = records.stream()
                .map(this::convert)
                .peek(vo -> {
                    OrderQueryVO orderQueryVO = packageIdMap.get(vo.getId());
                    if (orderQueryVO == null) return;
                    vo.setRelatedOrderId(orderQueryVO.getOrderId());
                    MemberVO memberVO = memberMap.get(orderQueryVO.getMemberId());
                    if (memberVO == null) return;
                    vo.setOrderCommodityName(memberVO.getName());
                })
                .collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public void save(Long templateId) {
        Long currentUserId = UserHolder.getUser().getId();
        UserCoupon userCoupon = UserCoupon.builder()
                .id(null)
                .userId(currentUserId)
                .couponTemplateId(templateId)
                .status(CouponStatus.NOT_USE)
                .createTime(null)
                .updateTime(null)
                .build();
        userCouponRepository.save(userCoupon);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void use(Long id, Long orderId, Long typeId, Long packageId) {
        this.validateCouponApplicability(id, typeId, packageId);
        distributedLockUtil.executeWithLock(CouponConstants.Cache.USE_LOCK + id, () -> {  // user_coupon:id 锁粒度最优
            transactionTemplate.executeWithoutResult(txStatus -> {
                UserCoupon userCouponById = userCouponRepository.findUserCouponById(id);
                if (userCouponById.isAvailable()) {
                    userCouponById.use(orderId);
                    userCouponRepository.save(userCouponById);
                } else {
                    throw new BusinessException("优惠券已使用！");
                }
            });
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refund(Long id) {
        if (id == null) {
            return;
        }
        UserCoupon userCoupon = userCouponRepository.findUserCouponById(id);
        if (userCoupon == null) {
            return;
        }

        if (userCoupon.getStatus() == CouponStatus.USE) {
            distributedLockUtil.executeWithLock(CouponConstants.Cache.REFUND_LOCK + id, () -> {  // user_coupon:id 锁粒度最优
                transactionTemplate.executeWithoutResult(txStatus -> {
                    UserCoupon userCouponById = userCouponRepository.findUserCouponById(id);
                    if (userCouponById.getStatus() == CouponStatus.USE) {
                        userCouponById.refund();
                        userCouponRepository.save(userCouponById);
                    }
                });
            });
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal calculateAmount(BigDecimal price, Integer quantity, Long couponId) {
        //总价
        BigDecimal totalAmount = price.multiply(new BigDecimal(quantity)).setScale(2, RoundingMode.HALF_UP);
        if (couponId == null) {
            return totalAmount;
        }

        // 查找用户持有的优惠券记录
        UserCoupon userCoupon = userCouponRepository.findUserCouponById(couponId);
        if (userCoupon == null) {
            return totalAmount;
        }

        // 获取对应的优惠券规则模板
        Coupon template = userCoupon.getTemplate();
        if (template == null) {
            return totalAmount;
        }

        // 计算优惠金额
        BigDecimal discount = template.getDiscount() != null ? template.getDiscount() : BigDecimal.ONE;
        BigDecimal minusAmount = template.getAmount() != null ? template.getAmount() : BigDecimal.ZERO;

        BigDecimal finalAmount = totalAmount.multiply(discount).subtract(minusAmount);

        // 保证优惠后金额最少为 0.01 元
        BigDecimal minAmount = new BigDecimal("0.01");
        return finalAmount.compareTo(minAmount) < 0 ? minAmount : finalAmount;
    }


    private void validateCouponApplicability(Long couponId, Long typeId, Long packageId) {
        if (couponId == null) {
            return;
        }
        UserCoupon userCoupon = userCouponRepository.findUserCouponById(couponId);
        if (userCoupon == null) {
            throw new ParameterException("未找到对应的优惠券记录，ID: " + couponId);
        }

        // 校验所属权
        Long currentUserId = UserHolder.getUser().getId();
        if (!userCoupon.getUserId().equals(currentUserId)) {
            throw new ParameterException("该优惠券不属于当前登录用户！");
        }

        // 校验使用状态
        if (!userCoupon.isAvailable()) {
            throw new ParameterException("该优惠券已使用或已失效！");
        }

        // 校验适用范围
        Coupon template = userCoupon.getTemplate();
        if (template != null) {
            String reason = template.isApplicable(typeId, packageId);
            if (!StringUtil.isNullOrEmpty(reason)) {
                throw new ParameterException(reason);
            }
        }
    }

    @Override
    public List<CouponQueryVO> queryCouponsForOrder(Long typeId, Long packageId) {
        Long currentUserId = UserHolder.getUser().getId();
        List<UserCoupon> unused = userCouponRepository.findUnusedByUserId(currentUserId);
        return unused.stream()
                .map(uc -> {
                    Coupon template = uc.getTemplate();
                    String reason;
                    if (!uc.isAvailable()) {
                        reason = "优惠券已经过期";
                    } else {
                        reason = template == null ? "未找到优惠券信息" : template.isApplicable(typeId, packageId);
                    }
                    return convert(uc, reason);
                })
                .collect(Collectors.toList());
    }

    private CouponQueryVO convert(UserCoupon userCoupon) {
        return convert(userCoupon, null);
    }

    private CouponQueryVO convert(UserCoupon userCoupon, String reason) {
        Coupon template = userCoupon.getTemplate();
        if (template == null) {
            throw new BusinessException("未找到对应的优惠券信息！");
        }
        return CouponQueryVO.builder()
                .id(userCoupon.getId())
                .name(template.getName())
                .amount(template.getAmount())
                .discount(template.getDiscount())
                .status(userCoupon.getStatus().getCode())
                .startTime(userCoupon.getCreateTime() != null ? userCoupon.getCreateTime().toLocalDateTime() : null)
                .endTime(userCoupon.getEndTime().toLocalDateTime())
                .validDays(template.getValidDays())
                .validHours(template.getValidHours())
                .description(template.getDescription())
                .relatedOrderId(userCoupon.getOrderId())
                .scopeType(template.getScopeType().getCode())
                .isAvailable(userCoupon.isAvailable())
                .reason(reason)
                .createTime(userCoupon.getCreateTime() != null ? userCoupon.getCreateTime().toLocalDateTime() : null)
                .build();
    }

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

        Map<Long, CouponUseScope> scopes = requestedScope == Coupon.CouponScopeType.ALL_SCOPE
                ? Map.of()
                : couponUseScopeRepository.findByCouponIds(couponIds);
        var currentUser = UserHolder.getUser();
        Long currentUserId = currentUser == null ? null : currentUser.getId();
        boolean hasReceiveLimits = activities.stream().anyMatch(activity -> activity.getLimitQuantity() != null);
        Map<Long, Integer> receivedCounts = currentUserId == null || !hasReceiveLimits
                ? Map.of()
                : userCouponRepository.countByUserIdAndCouponIds(currentUserId, couponIds);

        List<Long> relationIds = scopes.values().stream()
                .map(CouponUseScope::getRelationId)
                .filter(java.util.Objects::nonNull)
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
        return CouponActivityQueryVO.builder()
                .id(activity.getId())
                .couponId(activity.getCouponId())
                .name(activity.getName())
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
                .isAvailable(limitQuantity == null || receivedCount < limitQuantity)
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

    private <K, V> Map<K, V> emptyIfNull(Map<K, V> values) {
        return values == null ? Map.of() : values;
    }

    @Override

    public void receiveActivityCoupon(Long activityId) {
        Long currentUserId = UserHolder.getUser().getId();

        CouponActivity activity = couponActivityRepository.findById(activityId);
        if (activity == null) {
            throw new BusinessException("该优惠券活动不存在！");
        }
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
        couponCacheProvider.cacheUserLimit(currentUserId, couponId, activity.getDuration(now));
        boolean isOk = couponCacheProvider.deductStock(couponId, (long) activity.getLimitQuantity(), activityId);
        if (!isOk) {
            throw new BusinessException("优惠券库存不足！");
        }
        transactionTemplate.executeWithoutResult((status) -> {  //将缓存操作移动出去,减少事务持有时间
            try {
                userCouponRepository.save(userCoupon);
                log.info("优惠券:{} 领取成功 用户id:{}！", couponId, currentUserId);
            } catch (Exception e) {
                couponCacheProvider.increaseStock(couponId, currentUserId, activityId);
                log.error("优惠券:{} 领取失败 用户id:{}！", couponId, currentUserId, e);
                throw e;
            }
        });

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


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveActivity(CouponActivityDTO dto) {
        if (UserHolder.getUser().getAdmin() == 0) throw new UnPermissionException("无权限操作");
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

        couponCacheProvider.preWarmStock(activity);
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

    @Override
    public Map<Long, CouponQueryVO> queryByIds(List<Long> cList) {
        if (cList.isEmpty()) return Map.of();
        List<Coupon> byIds = couponRepository.findByIds(cList);
        return byIds.stream().collect(Collectors.toMap(Coupon::getId, this::toVO));
    }

    private CouponQueryVO toVO(Coupon coupon) {
        return CouponQueryVO.builder()
                .id(coupon.getId())
                .name(coupon.getName())
                .discount(coupon.getDiscount())
                .amount(coupon.getAmount())
                .status(coupon.getStatus())
                .validDays(coupon.getValidDays())
                .validHours(coupon.getValidHours())
                .scopeType(coupon.getScopeType().getCode())
                .reason(coupon.isApplicable(null, null))
                .description(coupon.getDescription())
                .build();
    }
}
