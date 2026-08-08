package com.summit.stp.activity.infrastructure.scheduler;

import com.summit.stp.activity.application.service.CouponActivityCacheProvider;
import com.summit.stp.activity.domain.repository.CouponActivityRepository;
import com.summit.stp.common.util.DistributedLockUtil;
import com.summit.stp.coupon.infrastructure.constants.CouponConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponStockScheduler {
    private final DistributedLockUtil distributedLockUtil;
    private final CouponActivityCacheProvider couponActivityCacheProvider;
    private final CouponActivityRepository couponActivityRepository;

    @Scheduled(cron = "0 0 0 * * ?")
    public void refreshCouponStock() {
        // 刷新优惠券库存
        distributedLockUtil.executeWithLock(CouponConstants.Cache.SCHEDULED_REFRESH_COUPON_STOCK, this::conduct);
    }

    private void conduct() {
        Map<Object, Object> stockOfActivities = null;
        try {
            // 查询所有未结束的活动库存
            stockOfActivities = couponActivityCacheProvider.getStockOfActivities();
            // 更新优惠券活动库存
            couponActivityRepository.batchUpdate(stockOfActivities);
        } catch (Exception e) {
            log.error("【优惠券活动】定时刷新库存失败，开始逐条降级更新！", e);
            if (stockOfActivities != null) {
                stockOfActivities.forEach((id, stock) -> {
                    try {
                        couponActivityRepository.updateStockByActivityId(id, stock);
                    } catch (Exception e1) {
                        log.error("【优惠券活动】降级刷新库存失败，ID: {}", id, e1);
                    }
                });
            }
        }
    }
}
