package com.summit.stp.user.infrastructure.listener;

import com.summit.stp.shared.domain.model.Password;
import com.summit.stp.shared.domain.model.PhoneNumber;
import com.summit.stp.shared.domain.model.Username;
import com.summit.stp.user.domain.exception.UserExistException;
import com.summit.stp.user.domain.model.User;
import com.summit.stp.user.domain.repository.UserRepository;
import com.summit.stp.userAuth.domain.event.UserRegisterEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
            value = @Queue(name = "user.queue.register", durable = "true"),
            exchange = @Exchange(name = "user.exchange.register", durable = "true"),
            key = "user.queue.register"
    ))
    public void onEvent(UserRegisterEvent event) {
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
        } catch (DuplicateKeyException e) {
            log.warn("【用户注册MQ监听】用户已存在: {}", event.getUsername());
            throw new UserExistException();
        }
    }
}
