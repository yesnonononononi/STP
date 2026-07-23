package com.summit.stp.user.infrastructure.listener;

import com.rabbitmq.client.Channel;
import com.summit.stp.shared.constants.MqConstants;
import com.summit.stp.shared.domain.model.Password;
import com.summit.stp.shared.domain.model.PhoneNumber;
import com.summit.stp.shared.domain.model.Username;
import com.summit.stp.user.domain.model.User;
import com.summit.stp.user.domain.repository.UserRepository;
import com.summit.stp.shared.domain.event.UserRegisterEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRegisterQueueListener {
    private final UserRepository userRepository;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MqConstants.User.QUEUE_REGISTER, durable = "true"),
            exchange = @Exchange(name = MqConstants.User.EXCHANGE, type = "topic", durable = "true"),
            key = MqConstants.User.ROUTING_KEY_REGISTER
    ))
    public void onEvent(UserRegisterEvent event, Message message, Channel channel) throws Exception {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        log.info("【用户注册MQ监听】收到用户注册事件: {}", event.getUsername());
        try {
            // 使用事件传来的唯一用户名
            Username username = Username.of(event.getUsername());
            // 使用哈希值还原密码对象
            Password password = Password.fromHash(event.getPasswordHash());
            PhoneNumber phoneNumber = PhoneNumber.of(event.getPhoneNumber());

            User user = User.builder().username(username).password(password).phoneNumber(phoneNumber).build();
            userRepository.save(user);
            log.info("【用户注册MQ监听】用户注册档案创建成功: {}", event.getUsername());
            channel.basicAck(deliveryTag, false);
        } catch (DuplicateKeyException e) {
            log.warn("【用户注册MQ监听】用户已存在: {}", event.getUsername());
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("【用户注册MQ监听】用户注册异常: {}", event.getUsername(), e);
            channel.basicNack(deliveryTag, false, true);
        }
    }
}
