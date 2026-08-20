package com.summit.stp.elasticsearch.listener;

import com.rabbitmq.client.Channel;
import com.summit.stp.common.application.domain.event.EsUserUpdateEvent;
import com.summit.stp.common.constants.MqConstants;
import com.summit.stp.elasticsearch.document.UserDocument;
import com.summit.stp.elasticsearch.repo.AdminUserQueryRepository;
import com.summit.stp.member.domain.model.UserMember;
import com.summit.stp.member.domain.repository.UserMemberRepository;
import com.summit.stp.user.domain.model.User;
import com.summit.stp.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEsUpdateListener {

    private final UserRepository<User> userRepository;
    private final UserMemberRepository userMemberRepository;
    private final AdminUserQueryRepository adminUserQueryRepository;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MqConstants.Es.QUEUE_USER_UPDATE, durable = "true"),
            exchange = @Exchange(name = MqConstants.Es.EXCHANGE, type = "topic"),
            key = MqConstants.Es.ROUTING_KEY_USER_UPDATE
    ))
    public void onUserEsUpdateEvent(EsUserUpdateEvent event, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        if (event == null || event.getUserId() == null) {
            channel.basicAck(deliveryTag, false);
            return;
        }
        Long userId = event.getUserId();
        log.info("【用户模块】收到系统ES用户更新事件, userId={}, eventType={}", userId, event.getEventType());
        try {
            if (event.getEventType() == EsUserUpdateEvent.EventType.DELETE) {
                adminUserQueryRepository.deleteById(userId);
                log.info("【用户模块】系统ES删除用户文档成功, userId={}", userId);
            } else {
                update(userId);
            }
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("【用户模块】系统ES同步更新用户数据异常, userId={}", userId, e);
            channel.basicNack(deliveryTag, false, false);
        }
    }

    private void update(Long userId) {
        // 根据 userId 从 DB 重新拉取最新完整数据并全量覆盖
        User user = userRepository.findUserById(userId).orElse(null);
        if (user == null) {
            log.warn("【用户模块】系统ES实时更新取消，数据库未找到用户, userId={}", userId);
            return;
        }

        UserMember userMember = userMemberRepository.queryUserMemberByUserId(userId);
        boolean isVip = userMember != null && userMember.isMemberActive();
        long vipLevel = (userMember != null && userMember.getLevel() != null && userMember.getLevel().getLevel() != null)
                ? userMember.getLevel().getLevel() : 0L;

        UserDocument doc = UserDocument.builder()
                .id(userId)
                .userId(userId)
                .nick(user.getNick() != null ? user.getNick() : "")
                .phone(user.getPhoneNumber() != null ? user.getPhoneNumber().getValue() : "")
                .ip(user.getIp() != null ? user.getIp() : "")
                .createTime(user.getCreateTime() != null ? Date.from(user.getCreateTime()) : new Date())
                .isVip(isVip)
                .age(user.getAge() != null ? user.getAge() : 0)
                .gender(user.getGender())
                .vipLevel((int) vipLevel)
                .statusCode(user.getStatusCode() != null ? user.getStatusCode() : 1)
                .build();

        adminUserQueryRepository.save(doc);
        log.info("【用户模块】系统ES全量覆盖更新用户文档成功, userId={}", userId);
    }
}
