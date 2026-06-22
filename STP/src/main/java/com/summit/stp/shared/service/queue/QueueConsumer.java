package com.summit.stp.shared.service.queue;

import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class QueueConsumer {
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "queue.order", durable = "true"),
            exchange = @Exchange(name = "order.topic.exchange", type = "topic"),
            key = "queue.order"
    )
    )
    public void receive(Object message) {
        System.out.println("接收到消息：" + message);
    }



}
