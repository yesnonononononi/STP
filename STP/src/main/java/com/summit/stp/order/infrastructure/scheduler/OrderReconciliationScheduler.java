package com.summit.stp.order.infrastructure.scheduler;

import com.summit.stp.order.application.service.OrderReconciliationAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderReconciliationScheduler {

    private final OrderReconciliationAppService orderReconciliationAppService;

    /**
     * 定时执行对账操作，每 5 分钟执行一次
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void executeReconciliation() {
        log.info("【定时任务】启动订单三方对账任务...");
        try {
            orderReconciliationAppService.reconcilePendingOrders();
            log.info("【定时任务】订单三方对账任务完成。");
        } catch (Exception e) {
            log.error("【定时任务】订单三方对账任务运行异常: ", e);
        }
    }
}
