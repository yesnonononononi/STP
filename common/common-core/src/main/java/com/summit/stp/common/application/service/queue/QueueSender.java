package com.summit.stp.common.application.service.queue;

import com.summit.stp.common.application.domain.event.UserChangedEvent;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class QueueSender {
    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送通用消息到 Exchange / RoutingKey
     */
    public void send(String exchange, String routingKey, Object message) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, message);
        } catch (Exception e) {
            log.error("【MQSender】标识：MQ 动作：发送消息失败, exchange={}, routingKey={}", exchange, routingKey, e);
        }
    }

    /**
     * 发送用户关键状态更新事件到 ES 队列（直接传递 Long userId）
     *
     * @param exchange   交换机
     * @param routingKey 路由键
     * @param userId     用户ID
     */
    public void sendUpdateEvent(String exchange, String routingKey, @NonNull Long userId) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, userId);
            log.info("【管理员服务】标识：MQ 动作：已发布用户关键状态更新事件到 ES 队列, userId={}", userId);
        } catch (Exception e) {
            log.error("【管理员服务】标识：MQ 动作：发布用户关键状态更新事件失败, userId={}", userId, e);
        }
    }

    /**
     * 发送用户注册/创建生命周期事件
     *
     * @param exchange   交换机
     * @param routingKey 路由键
     * @param event      用户变更事件
     */
    public void sendRegisterEvent(String exchange, String routingKey, @NonNull UserChangedEvent event) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
            log.info("【用户注册】标识：MQ 动作：已发布用户注册事件, userId={}", event.getUserId());
        } catch (Exception e) {
            log.error("【用户注册】标识：MQ 动作：发布用户注册事件失败, userId={}", event.getUserId(), e);
        }
    }
}
