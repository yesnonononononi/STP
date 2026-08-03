package com.summit.stp.coupon.infrastructure.scheduler;

import com.summit.stp.common.util.DistributedLockUtil;
import com.summit.stp.coupon.domain.repository.CouponActivityRepository;
import com.summit.stp.coupon.domain.service.CouponCacheProvider;
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
    private final CouponCacheProvider couponCacheProvider;
    private final CouponActivityRepository couponActivityRepository;


    @Scheduled(cron = "0 0 0 * * ?")
    public void refreshCouponStock() {
        // 刷新优惠券库存
        distributedLockUtil.executeWithLock(CouponConstants.Cache.SCHEDULED_REFRESH_COUPON_STOCK, this::conduct);
    }
    private void conduct(){
        Map<Object, Object> stockOfActivities = null;
        try {
            //查询所有未结束的活动
              stockOfActivities = couponCacheProvider.getStockOfActivities();
            //更新优惠券库存
            couponActivityRepository.batchUpdate(stockOfActivities);
        }catch (Exception e){
            log.error("【定时刷新优惠券库存】刷新优惠券库存失败,开始逐条更新！");
            if(stockOfActivities != null){
                stockOfActivities.forEach((id, stock) -> {
                    try {
                        couponActivityRepository.updateStockByActivityId(id, stock);
                    }catch (Exception e1){
                        log.error("【定时刷新优惠券库存-降级】刷新优惠券库存失败,ID: {}", id, e1);
                    }
                });
            }
        }

    }
}
