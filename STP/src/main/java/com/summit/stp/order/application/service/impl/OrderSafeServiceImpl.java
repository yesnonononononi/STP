package com.summit.stp.order.application.service.impl;

import com.summit.stp.order.application.service.OrderAppService;
import com.summit.stp.order.application.service.OrderSafeService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.sql.Timestamp;
import java.util.Set;

@Service
public class OrderSafeServiceImpl implements OrderSafeService {
    private final StringRedisTemplate stringRedisTemplate;
    private final String ORDER_TIMEOUT_KEY = "order:timeout:";
    private final OrderAppService orderAppService;

    public OrderSafeServiceImpl(StringRedisTemplate stringRedisTemplate, @Lazy OrderAppService orderAppService) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.orderAppService = orderAppService;
    }


    @Override
    public void processOrderTimeout(Long duration, Long orderId) {
        //1,计算超时时间
        Timestamp timeout = new Timestamp(System.currentTimeMillis() + duration);
        //redis zset存入超时时间 (使用公共Key，以orderId为member，以时间戳为score)
        ZSetOperations<String, String> zset = stringRedisTemplate.opsForZSet();
        zset.add(ORDER_TIMEOUT_KEY, String.valueOf(orderId), (double) timeout.getTime());
    }

    @Override
    public void removeOrderTimeout(Long orderId) {
        ZSetOperations<String, String> zset = stringRedisTemplate.opsForZSet();
        zset.remove(ORDER_TIMEOUT_KEY, String.valueOf(orderId));
    }
}
