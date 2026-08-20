package com.summit.stp.elasticsearch.listener;

import com.rabbitmq.client.Channel;
import com.summit.stp.common.application.domain.event.PostChangeEvent;
import com.summit.stp.common.constants.MqConstants;
import com.summit.stp.elasticsearch.document.PostDocument;
import com.summit.stp.elasticsearch.repo.EsPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostChangeEventListener {
    private final EsPostRepository esPostRepository;
    private final JsonMapper objectMapper;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MqConstants.Post.QUEUE_CHANGE),
            exchange = @Exchange(name = MqConstants.Post.EXCHANGE, type = "topic"),
            key = MqConstants.Post.ROUTING_KEY_CHANGE
    ))
    public void consume(PostChangeEvent postD, Channel channel, Message message) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        log.info("【ES模块】收到帖子更改事件: {}",postD);
        try {
            // 1. 条件判断
            if (postD == null || postD.getData() == null) {
                log.error("【ES模块】动作：消费帖子变更事件, 消息为空: {}", Objects.toString(postD, "null"));
                channel.basicAck(deliveryTag, false);
                return;
            }
            // 2. 执行操作
            action(postD);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("【ES模块】动作：消费帖子变更事件异常, postD={}", postD, e);
            try {
                // 不重新入队 (requeue=false)，防止格式异常/数据错误导致死循环卡死 MQ 队列
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ie) {
                log.error("【ES模块】动作：消息拒绝入队失败, postD={}", postD, ie);
            }
        }
    }

    private void action(PostChangeEvent postD) {
        PostChangeEvent.EventType eventType = postD.getEventType();
        PostDocument doc = parseDocument(postD.getData());
        if (doc == null) return;

        switch (eventType) {
            case CREATE, UPDATE -> esPostRepository.save(doc);
            case DELETE -> {
                if (doc.getPostId() != null) {
                    esPostRepository.deleteById(doc.getPostId());
                }
            }
        }
    }

    private PostDocument parseDocument(Object data) {
        if (data instanceof PostDocument doc) {
            return doc;
        }
        if (data != null) {
            try {
                return objectMapper.convertValue(data, PostDocument.class);
            } catch (Exception e) {
                log.error("【ES模块】动作：解析转换 PostDocument 失败, data={}", data, e);
            }
        }
        return null;
    }
}


