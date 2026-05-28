package com.summit.stp.coupon.application.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.coupon.application.service.CouponAppService;
import com.summit.stp.coupon.application.vo.CouponQueryVO;
import com.summit.stp.coupon.domain.model.Coupon;
import com.summit.stp.coupon.domain.model.UserCoupon;
import com.summit.stp.coupon.domain.model.CouponStatus;
import com.summit.stp.coupon.domain.repository.CouponRepository;
import com.summit.stp.coupon.domain.repository.UserCouponRepository;
import com.summit.stp.shared.ThreadContext.UserHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.summit.stp.shared.exception.ParameterException;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponAppServiceImpl implements CouponAppService {
    private final UserCouponRepository userCouponRepository;
    private final CouponRepository couponRepository;

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
    public Page<CouponQueryVO> queryHistory(long page, long pageSize) {
        Long currentUserId = UserHolder.getUser().getId();
        Page<UserCoupon> userCouponPage = userCouponRepository.queryHistoryByUser(currentUserId, page, pageSize);

        Page<CouponQueryVO> voPage = new Page<>(userCouponPage.getCurrent(), userCouponPage.getSize(), userCouponPage.getTotal());

        List<CouponQueryVO> voList = userCouponPage.getRecords().stream()
                .map(this::convert)
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
    public void use(Long id, Long orderId) {
        if (id == null) {
            return;
        }
        UserCoupon userCoupon = userCouponRepository.findUserCouponById(id);
        if (userCoupon == null) {
            throw new ParameterException("未找到对应的优惠券记录，ID: " + id);
        }

        // 校验所属权
        Long currentUserId = UserHolder.getUser().getId();
        if (!userCoupon.getUserId().equals(currentUserId)) {
            throw new ParameterException("该优惠券不属于当前登录用户！");
        }

        userCoupon.use(orderId);
        Coupon template = userCoupon.getTemplate();
        if (template != null) {
            template.deductStock();
        }
        userCouponRepository.save(userCoupon);
        if (template != null) {
            couponRepository.update(template);
        }
    }

    @Override
    public void refund(Long id) {
        if (id == null) {
            return;
        }
        UserCoupon userCoupon = userCouponRepository.findUserCouponById(id);
        if (userCoupon == null) {
            return;
        }

        if (userCoupon.getStatus() == CouponStatus.USE) {
            userCoupon.refund();
            Coupon template = userCoupon.getTemplate();
            if (template != null) {
                template.increaseStock();
            }
            userCouponRepository.save(userCoupon);
            if (template != null) {
                couponRepository.update(template);
            }
        }
    }


    @Override
    public BigDecimal calculateAmount(BigDecimal price, Integer quantity, Long couponId) {
        //总价
        BigDecimal totalAmount = price.multiply(new BigDecimal(quantity));
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

        // 保证优惠后金额不能为负数
        return finalAmount.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : finalAmount;
    }

    private CouponQueryVO convert(UserCoupon userCoupon) {
        Coupon template = userCoupon.getTemplate();
        return CouponQueryVO.builder()
                .id(userCoupon.getId())
                .name(template != null ? template.getName() : "未知优惠券")
                .amount(template != null ? template.getAmount() : null)
                .discount(template != null ? template.getDiscount() : null)
                .status(userCoupon.getStatus())
                .createTime(userCoupon.getCreateTime())
                .updateTime(userCoupon.getUpdateTime())
                .build();
    }
}
