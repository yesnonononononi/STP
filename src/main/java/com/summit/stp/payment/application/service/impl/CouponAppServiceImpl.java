package com.summit.stp.payment.application.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.payment.application.service.CouponAppService;
import com.summit.stp.payment.application.vo.CouponQueryVO;
import com.summit.stp.payment.domain.model.Coupon;
import com.summit.stp.payment.domain.model.UserCoupon;
import com.summit.stp.payment.domain.repository.UserCouponRepository;
import com.summit.stp.shared.ThreadContext.UserHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponAppServiceImpl implements CouponAppService {
    private final UserCouponRepository userCouponRepository;

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
        UserCoupon userCoupon = UserCoupon.of(
                null, // 自增主键
                currentUserId,
                templateId,
                1, // 状态: 未使用 (NOT_USE)
                null,
                null
        );
        userCouponRepository.save(userCoupon);
    }

    @Override
    public void use(Long id) {
        UserCoupon userCoupon = userCouponRepository.findUserCouponById(id);
        if (userCoupon == null) {
            throw new IllegalArgumentException("未找到对应的优惠券记录，ID: " + id);
        }
        
        // 校验所属权
        Long currentUserId = UserHolder.getUser().getId();
        if (!userCoupon.getUserId().equals(currentUserId)) {
            throw new IllegalArgumentException("该优惠券不属于当前登录用户！");
        }
        
        userCoupon.use();
        userCouponRepository.save(userCoupon);
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
