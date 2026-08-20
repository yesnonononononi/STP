package com.summit.stp.user_coupon.application.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.common.auth.UserHolder;
import com.summit.stp.coupon.api.vo.CouponQueryVO;
import com.summit.stp.user.api.vo.MemberVO;
import com.summit.stp.order.api.vo.OrderQueryVO;
import com.summit.stp.common.application.domain.exception.BusinessException;
import com.summit.stp.common.application.domain.exception.ParameterException;
import com.summit.stp.user.api.client.MemberFeignClient;
import com.summit.stp.order.api.client.OrderFeignClient;
import com.summit.stp.common.util.DistributedLockUtil;
import com.summit.stp.coupon.domain.model.Coupon;
import com.summit.stp.coupon.domain.repository.CouponRepository;
import com.summit.stp.coupon.infrastructure.constants.CouponConstants;
import com.summit.stp.user_coupon.application.service.UserCouponAppService;
import com.summit.stp.user_coupon.domain.model.CouponStatus;
import com.summit.stp.user_coupon.domain.model.UserCoupon;
import com.summit.stp.user_coupon.domain.repository.UserCouponRepository;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCouponAppServiceImpl implements UserCouponAppService {
    private final UserCouponRepository userCouponRepository;
    private final OrderFeignClient orderFeignClient;
    private final MemberFeignClient memberFeignClient;
    private final DistributedLockUtil distributedLockUtil;
    private final TransactionTemplate transactionTemplate;

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
        if (ids.isEmpty()) {
            voPage.setRecords(List.of());
            return voPage;
        }

        // 查询优惠券被订单使用信息
        Map<Long, OrderQueryVO> packageIdMap = orderFeignClient.findOrderByCouponIds(currentUserId, ids).getData();
        List<Long> packageIds = packageIdMap != null ? packageIdMap.values().stream().map(OrderQueryVO::getMemberId).toList() : List.of();
        Map<Long, MemberVO> memberMap = packageIds.isEmpty() ? Map.of() : memberFeignClient.queryMemberByIds(packageIds).getData();

        List<CouponQueryVO> voList = records.stream()
                .map(this::convert)
                .peek(vo -> {
                    if (packageIdMap == null) return;
                    OrderQueryVO orderQueryVO = packageIdMap.get(vo.getId());
                    if (orderQueryVO == null) return;
                    vo.setRelatedOrderId(orderQueryVO.getOrderId());
                    if (memberMap == null) return;
                    MemberVO memberVO = memberMap.get(orderQueryVO.getMemberId());
                    if (memberVO == null) return;
                    vo.setOrderCommodityName(memberVO.getName());
                })
                .collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void use(Long id, Long orderId, Long typeId, Long packageId) {
        this.validateCouponApplicability(id, typeId, packageId);
        distributedLockUtil.executeWithLock(CouponConstants.Cache.USE_LOCK + id, () -> {
            transactionTemplate.executeWithoutResult(txStatus -> {
                UserCoupon userCouponById = userCouponRepository.findUserCouponById(id);
                if (userCouponById != null && userCouponById.isAvailable()) {
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
            distributedLockUtil.executeWithLock(CouponConstants.Cache.REFUND_LOCK + id, () -> {
                transactionTemplate.executeWithoutResult(txStatus -> {
                    UserCoupon userCouponById = userCouponRepository.findUserCouponById(id);
                    if (userCouponById != null && userCouponById.getStatus() == CouponStatus.USE) {
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
        BigDecimal totalAmount = price.multiply(new BigDecimal(quantity)).setScale(2, RoundingMode.HALF_UP);
        if (couponId == null) {
            return totalAmount;
        }

        UserCoupon userCoupon = userCouponRepository.findUserCouponById(couponId);
        if (userCoupon == null) {
            return totalAmount;
        }

        Coupon template = userCoupon.getTemplate();
        if (template == null) {
            return totalAmount;
        }

        BigDecimal discount = template.getDiscount() != null ? template.getDiscount() : BigDecimal.ONE;
        BigDecimal minusAmount = template.getAmount() != null ? template.getAmount() : BigDecimal.ZERO;

        BigDecimal finalAmount = totalAmount.multiply(discount).subtract(minusAmount);
        BigDecimal minAmount = new BigDecimal("0.01");
        return finalAmount.compareTo(minAmount) < 0 ? minAmount : finalAmount;
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
                        if (uc.getStatus() != CouponStatus.NOT_USE || uc.getUsedTime() != null) {
                            reason = "优惠券已使用";
                        } else {
                            reason = "优惠券已过期";
                        }
                    } else {
                        reason = template == null ? "未找到优惠券信息" : template.isApplicable(typeId, packageId);
                    }
                    return convert(uc, reason);
                })
                .collect(Collectors.toList());
    }

    private void validateCouponApplicability(Long couponId, Long typeId, Long packageId) {
        if (couponId == null) {
            return;
        }
        UserCoupon userCoupon = userCouponRepository.findUserCouponById(couponId);
        if (userCoupon == null) {
            throw new ParameterException("未找到对应的优惠券记录，ID: " + couponId);
        }

        Long currentUserId = UserHolder.getUser().getId();
        if (!userCoupon.getUserId().equals(currentUserId)) {
            throw new ParameterException("该优惠券不属于当前登录用户！");
        }

        if (!userCoupon.isAvailable()) {
            throw new ParameterException("该优惠券已使用或已失效！");
        }

        Coupon template = userCoupon.getTemplate();
        if (template != null) {
            String reason = template.isApplicable(typeId, packageId);
            if (!StringUtil.isNullOrEmpty(reason)) {
                throw new ParameterException(reason);
            }
        }
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
                .status(userCoupon.getStatus() != null ? userCoupon.getStatus().getCode() : null)
                .startTime(userCoupon.getCreateTime() != null ? userCoupon.getCreateTime().toLocalDateTime() : null)
                .endTime(userCoupon.getEndTime() != null ? userCoupon.getEndTime().toLocalDateTime() : null)
                .validDays(template.getValidDays())
                .validHours(template.getValidHours())
                .description(template.getDescription())
                .relatedOrderId(userCoupon.getOrderId())
                .scopeType(template.getScopeType() != null ? template.getScopeType().getCode() : null)
                .isAvailable(userCoupon.isAvailable())
                .reason(reason)
                .createTime(userCoupon.getCreateTime() != null ? userCoupon.getCreateTime().toLocalDateTime() : null)
                .build();
    }


}

