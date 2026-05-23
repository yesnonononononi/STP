package com.summit.stp.order.infrastructure.scheduler;

import com.summit.stp.order.application.service.OrderAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutScheduler {

    private final StringRedisTemplate stringRedisTemplate;
    private final OrderAppService orderAppService;
    private final String ORDER_TIMEOUT_KEY = "order:timeout:";

    /**
     * 定时检查未支付超时订单，每 5 秒执行一次
     */
    @Scheduled(cron = "0/5 * * * * ?")
    public void checkOrderTimeout() {
        ZSetOperations<String, String> zset = stringRedisTemplate.opsForZSet();
        synchronized (this) {
            Set<String> orderId = zset.rangeByScore(ORDER_TIMEOUT_KEY, 0, System.currentTimeMillis());
            if (orderId != null && !orderId.isEmpty()) {
                log.info("【定时任务】发现超时未支付订单: {}", orderId);
                orderAppService.timeoutOrder(orderId);
                zset.remove(ORDER_TIMEOUT_KEY, orderId.toArray());
            }
        }
    }
}
