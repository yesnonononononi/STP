package com.summit.stp.user.infrastructure.listener;

import com.rabbitmq.client.Channel;
import com.summit.stp.common.application.domain.event.PostChangeEvent;
import com.summit.stp.common.application.domain.event.UserFansChangeEvent;
import com.summit.stp.common.application.domain.event.UserLikedChangeEvent;
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
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserStatQueueListener {
    private final UserStatMapper userStatMapper;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MqConstants.User.QUEUE_LIKED, durable = "true"),
            exchange = @Exchange(name = MqConstants.User.EXCHANGE, type = "topic"),
            key = MqConstants.User.ROUTING_KEY_LIKED
    ))
    public void onEvent(UserLikedChangeEvent event, Message message, Channel channel) throws Exception {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        Long userId = event.getUserId();
        Integer delta = event.getLikedDelta();
        if (userId == null || delta == null || delta == 0) {
            channel.basicAck(deliveryTag, false);
            return;
        }

        log.info("【用户获赞统计MQ监听】收到点赞数变更: userId={}, delta={}", userId, delta);
        try {
            // 1. 尝试原子更新 liked 字段
            int rows = userStatMapper.incrLiked(userId, delta);
            if (rows == 0) {
                // 2. 更新行数为0，说明 user_stat 表中该 userId 尚未初始化记录，进行兜底初始化插入

                try {
                    long liked = Math.max(0L, delta.longValue());
                    initUserStat(userId, 0, 0, liked);
                    log.info("【用户获赞统计MQ监听】已为用户创建初始统计档案，并初始化 liked: userId={}, liked={}", userId, liked );
                } catch (DuplicateKeyException e) {
                    // 3. 并发插入冲突（其它消费线程已抢先插入），重新执行原子累加
                    userStatMapper.incrLiked(userId, delta);
                    log.info("【用户获赞统计MQ监听】并发插入冲突，已回退为原子累加更新: userId={}, delta={}", userId, delta);
                }
            } else {
                log.info("【用户获赞统计MQ监听】原子更新 liked 成功: userId={}, delta={}", userId, delta);
            }
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("【用户获赞统计MQ监听】更新用户获赞统计失败: userId={}, delta={}", userId, delta, e);
            channel.basicNack(deliveryTag, false, true);
        }
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MqConstants.User.QUEUE_FANS, durable = "true"),
            exchange = @Exchange(name = MqConstants.User.EXCHANGE, type = "topic"),
            key = MqConstants.User.ROUTING_KEY_FANS
    ))
    public void onFansEvent(UserFansChangeEvent event, Message message, Channel channel) throws Exception {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        Long userId = event.getUserId();
        Integer delta = event.getFansDelta();
        if (userId == null || delta == null || delta == 0) {
            channel.basicAck(deliveryTag, false);
            return;
        }

        log.info("【用户粉丝统计MQ监听】收到粉丝数变更: userId={}, delta={}", userId, delta);
        try {
            // 1. 尝试原子更新 fans 字段
            int rows = userStatMapper.incrFans(userId, delta);
            if (rows == 0) {
                // 2. 兜底初始化插入
                try {
                    long fans = Math.max(0L, delta.longValue());
                    initUserStat(userId, fans, 0, 0);
                    log.info("【用户粉丝统计MQ监听】已为用户创建初始统计档案，并初始化 fans: userId={}, fans={}", userId, fans);
                } catch (DuplicateKeyException e) {
                    // 3. 并发冲突重试
                    userStatMapper.incrFans(userId, delta);
                    log.info("【用户粉丝统计MQ监听】并发插入冲突，已回退为原子累加更新: userId={}, delta={}", userId, delta);
                }
            } else {
                log.info("【用户粉丝统计MQ监听】原子更新 fans 成功: userId={}, delta={}", userId, delta);
            }
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("【用户粉丝统计MQ监听】更新用户粉丝统计失败: userId={}, delta={}", userId, delta, e);
            channel.basicNack(deliveryTag, false, true);
        }
    }
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MqConstants.User.QUEUE_TOPIC, durable = "true"),
            exchange = @Exchange(name = MqConstants.Post.EXCHANGE, type = "topic"),
            key = MqConstants.Post.ROUTING_KEY_CHANGE
    ))

    public void onTopicEvent(PostChangeEvent event, Message message, Channel channel) throws Exception {

        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        if(!event.getEventType().equals(PostChangeEvent.EventType.CREATE)){
            channel.basicAck(deliveryTag, false);
            return;
        }
        Long userId = event.getUid();
        Integer delta = 1;
        if (userId == null) {
            channel.basicAck(deliveryTag, false);
        }
        log.info("【用户话题统计MQ监听】收到话题数变更: userId={}, delta={}", userId, delta);
        try{
            int difference = userStatMapper.incrTopic(userId, delta);
            if (difference == 0) {
                initUserStat(userId, 0, delta, 0);
            }
            channel.basicAck(deliveryTag, false);
        }catch (Exception e){
            log.error("【用户话题统计MQ监听】更新用户话题统计失败: userId={}, delta={}", userId, delta, e);
            try {
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException ioException) {
                log.error("【用户话题统计MQ监听】更新用户话题统计-MQ应答失败", ioException);
            }
        }

    }

    public void initUserStat(Long userId,long fans,long topic,long liked){
        userStatMapper.insert(UserStatPO.builder()
                .userId(userId)
                .fans(fans)
                .topic(topic)
                .liked(liked)
                .build());
    }
}
