package com.summit.stp.order.application.service.impl;

import com.summit.stp.order.application.service.OrderAppService;
import com.summit.stp.order.application.service.OrderTimeoutProvider;
import com.summit.stp.shared.util.DistributedLockUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Set;

/**
 * 订单超时处理的 Redis ZSet 轮询实现
 */
@Slf4j
@Service
public class RedisOrderTimeoutProvider implements OrderTimeoutProvider {
    private final StringRedisTemplate stringRedisTemplate;
    private final OrderAppService orderAppService;
    private final String ORDER_TIMEOUT_KEY = "order:timeout:";
    private final String ORDER_TIMEOUT_KEY_LOCK = "order:timeout:lock";
    private final DistributedLockUtil distributedLockUtil;

    public RedisOrderTimeoutProvider(StringRedisTemplate stringRedisTemplate, @Lazy OrderAppService orderAppService, DistributedLockUtil distributedLockUtil) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.orderAppService = orderAppService;
        this.distributedLockUtil = distributedLockUtil;
    }

    @Override
    public void registerTimeout(Long duration, Long orderId) {
        Timestamp timeout = new Timestamp(System.currentTimeMillis() + duration);
        ZSetOperations<String, String> zset = stringRedisTemplate.opsForZSet();
        zset.add(ORDER_TIMEOUT_KEY, String.valueOf(orderId), (double) timeout.getTime());
    }

    @Override
    public void cancelTimeout(Long orderId) {
        ZSetOperations<String, String> zset = stringRedisTemplate.opsForZSet();
        zset.remove(ORDER_TIMEOUT_KEY, String.valueOf(orderId));
    }

    /**
     * 定时检查未支付超时订单，每 5 秒执行一次
     */
    @Scheduled(cron = "0/5 * * * * ?")
    public void checkOrderTimeout() {
        ZSetOperations<String, String> zset = stringRedisTemplate.opsForZSet();
        distributedLockUtil.executeWithLock(ORDER_TIMEOUT_KEY_LOCK, () -> {
            Set<String> orderIds = zset.rangeByScore(ORDER_TIMEOUT_KEY, 0, System.currentTimeMillis());
            if (orderIds != null && !orderIds.isEmpty()) {
                log.info("【Redis超时检测】发现超时未支付订单: {}", orderIds);
                orderAppService.timeoutOrder(orderIds);
                zset.remove(ORDER_TIMEOUT_KEY, orderIds.toArray());
            }
        });

    }
}

