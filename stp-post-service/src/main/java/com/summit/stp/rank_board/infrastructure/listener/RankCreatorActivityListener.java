package com.summit.stp.rank_board.infrastructure.listener;

import com.rabbitmq.client.Channel;
import com.summit.stp.common.application.domain.event.UserChangedEvent;
import com.summit.stp.common.constants.MqConstants;
import com.summit.stp.rank_board.application.service.CreatorRankBufferManager;
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
     * 监听统一的用户变更事件队列，更新创作者周榜积分
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MqConstants.Rank.QUEUE_CREATOR_ACTIVITY, durable = "true"),
            exchange = @Exchange(name = MqConstants.User.EXCHANGE, type = "topic"),
            key = MqConstants.User.ROUTING_KEY_CHANGE
    ))
    public void listenUserChange(UserChangedEvent event, Channel channel, Message message) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            channel.basicAck(deliveryTag, false);
            Long userId = event.getUserId();
            Double scoreDelta = event.getScoreDelta();

            if (userId != null && scoreDelta != null && scoreDelta != 0.0) {
                creatorRankBufferManager.incrementScore(userId, scoreDelta);
                log.info("【CreatorRank】标识：UserChange 动作：收到统一用户变更事件，自增创作者积分成功, userId={}, scoreDelta={}", userId, scoreDelta);
            }
        } catch (Exception e) {
            handleNack(channel, deliveryTag, "UserChange", e);
        }
    }

    /**
     * 统一的异常 Nack 处理
     */
    private void handleNack(Channel channel, long deliveryTag, String eventType, Exception e) {
        try {
            channel.basicNack(deliveryTag, false, true);
        } catch (IOException ioException) {
            log.error("【CreatorRank】标识：Listener 动作：异常应答nack失败, type={}", eventType, ioException);
        }
        log.error("【CreatorRank】标识：Listener 动作：处理异常, type={}", eventType, e);
    }
}
