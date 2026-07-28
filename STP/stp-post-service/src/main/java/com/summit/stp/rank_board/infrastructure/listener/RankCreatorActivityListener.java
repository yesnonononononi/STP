package com.summit.stp.rank_board.infrastructure.listener;

import com.rabbitmq.client.Channel;
import com.summit.stp.post.infrastructure.constants.PostConstants;
import com.summit.stp.rank_board.application.service.CreatorRankBufferManager;
import com.summit.stp.common.constants.MqConstants;
import com.summit.stp.common.application.domain.event.UserFansChangeEvent;
import com.summit.stp.common.application.domain.event.UserLikedChangeEvent;
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
public class RankCreatorActivityListener {

    private final CreatorRankBufferManager creatorRankBufferManager;

    /**
     * 监听点赞变更事件队列，累加点赞对应的创作者热度值
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MqConstants.Rank.QUEUE_CREATOR_LIKED, durable = "true"),
            exchange = @Exchange(name = MqConstants.User.EXCHANGE, type = "topic"),
            key = MqConstants.User.ROUTING_KEY_LIKED
    ))
    public void listenLikedChange(UserLikedChangeEvent event, Channel channel, Message message) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            channel.basicAck(deliveryTag, false);
            Long userId = event.getUserId();
            Integer delta = event.getLikedDelta();
            
            if (userId != null && delta != null) {
                double scoreDelta = delta * PostConstants.Business.CREATOR_SCORE_LIKE;
                creatorRankBufferManager.incrementScore(userId, scoreDelta);
                log.info("【CreatorRank】点赞监听 收到点赞变更, userId={}, delta={}, score={}", userId, delta, scoreDelta);
            }
        } catch (Exception e) {
            handleNack(channel, deliveryTag, "点赞", e);
        }
    }

    /**
     * 监听粉丝变更事件队列
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MqConstants.Rank.QUEUE_CREATOR_FANS, durable = "true"),
            exchange = @Exchange(name = MqConstants.User.EXCHANGE, type = "topic"),
            key = MqConstants.User.ROUTING_KEY_FANS
    ))
    public void listenFansChange(UserFansChangeEvent event, Channel channel, Message message) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            channel.basicAck(deliveryTag, false);
            Long userId = event.getUserId();
            Integer delta = event.getFansDelta();
            if (userId != null && delta != null) {
                double scoreDelta = delta * PostConstants.Business.CREATOR_SCORE_FAN;
                creatorRankBufferManager.incrementScore(userId, scoreDelta);
                log.info("【CreatorRank】粉丝监听 收到粉丝变更, userId={}, delta={}, score={}", userId, delta, scoreDelta);
            }
        } catch (Exception e) {
            handleNack(channel, deliveryTag, "粉丝", e);
        }
    }

    /**
     * 统一的异常 Nack 处理
     */
    private void handleNack(Channel channel, long deliveryTag, String eventType, Exception e) {
        try {
            channel.basicNack(deliveryTag, false, true);
        } catch (IOException ioException) {
            log.error("【CreatorRank】监听器 异常应答nack失败: type={}", eventType, ioException);
        }
        log.error("【CreatorRank】监听器 处理异常: type={}", eventType, e);
    }
}
