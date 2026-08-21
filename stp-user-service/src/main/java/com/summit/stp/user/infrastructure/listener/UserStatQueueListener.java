package com.summit.stp.user.infrastructure.listener;

import com.rabbitmq.client.Channel;
import com.summit.stp.common.application.domain.event.PostChangeEvent;
import com.summit.stp.common.application.domain.event.UserChangedEvent;
import com.summit.stp.common.constants.MqConstants;
import com.summit.stp.user.infrastructure.persistence.mapper.UserStatMapper;
import com.summit.stp.user.infrastructure.persistence.po.UserStatPO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserStatQueueListener {
    private final UserStatMapper userStatMapper;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MqConstants.User.QUEUE_TOPIC, durable = "true"),
            exchange = @Exchange(name = MqConstants.Post.EXCHANGE, type = "topic"),
            key = MqConstants.Post.ROUTING_KEY_CHANGE
    ))
    public void onTopicEvent(PostChangeEvent event, Message message, Channel channel) throws Exception {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        if (!event.getEventType().equals(PostChangeEvent.EventType.CREATE)) {
            channel.basicAck(deliveryTag, false);
            return;
        }
        Long userId = event.getUid();
        Integer delta = 1;
        if (userId == null) {
            channel.basicAck(deliveryTag, false);
            return;
        }
        log.info("【用户关系】标识：MQ 动作：收到话题数变更, userId={}, delta={}", userId, delta);
        try {
            int difference = userStatMapper.incrTopic(userId, delta);
            if (difference == 0) {
                initUserStat(userId, 0, delta, 0);
            }
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("【用户关系】标识：MQ 动作：更新用户话题统计失败: userId={}, delta={}", userId, delta, e);
            throw e;
        }
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MqConstants.User.QUEUE_LIKED, durable = "true"),
            exchange = @Exchange(name = MqConstants.User.EXCHANGE, type = "topic"),
            key = MqConstants.User.ROUTING_KEY_LIKED
    ))
    public void onLikedEvent(UserChangedEvent event, Message message, Channel channel) throws Exception {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        Long userId = event.getUserId();
        Object data = event.getData();
        if (userId == null || !(data instanceof Integer delta)) {
            channel.basicAck(deliveryTag, false);
            return;
        }

        log.info("【用户统计】标识：MQ 动作：收到点赞获赞数变更, userId={}, delta={}", userId, delta);
        try {
            int difference = userStatMapper.incrLiked(userId, delta);
            if (difference == 0) {
                initUserStat(userId, 0, 0, delta);
            }
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("【用户统计】标识：MQ 动作：更新用户获赞统计失败: userId={}, delta={}", userId, delta, e);
            try {
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException ioException) {
                log.error("【用户统计】标识：MQ 动作：更新用户获赞统计-MQ应答失败", ioException);
            }
        }
    }

    public void initUserStat(Long userId, long fans, long topic, long liked) {
        userStatMapper.insert(UserStatPO.builder()
                .userId(userId)
                .fans(fans)
                .topic(topic)
                .liked(liked)
                .build());
    }
}
