package com.summit.stp.activity.application.service.impl;

import com.summit.stp.activity.domain.event.SeckillEvent;
import com.summit.stp.common.constants.MqConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
@RequiredArgsConstructor
@Component
@Slf4j
public class CouponQueueSender {
    private final RabbitTemplate rabbitTemplate;

    public void send(SeckillEvent event){
        rabbitTemplate.convertAndSend(MqConstants.Coupon.EXCHANGE,MqConstants.Coupon.COUPON_SECKILL_ROUTING_KEY,event);
        log.info("【优惠券异步消息】event: {}", event);
    }

}
