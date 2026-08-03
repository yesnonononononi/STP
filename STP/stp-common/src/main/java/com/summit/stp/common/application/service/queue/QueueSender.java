package com.summit.stp.common.application.service.queue;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueueSender {
    private final RabbitTemplate rabbitTemplate;


    /**
     * 发送消息到队列
     * @param routingKey 路由键
     * @param message 消息体
     */
    public void send(String exchange,String routingKey,Object message){
        rabbitTemplate.convertAndSend(exchange,routingKey,message);
    }
}
