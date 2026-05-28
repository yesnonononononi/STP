package com.summit.stp.userAuth.infrastructure.publisher;

import com.summit.stp.userAuth.application.service.UserRegisterEventPublishProvider;
import com.summit.stp.userAuth.domain.event.UserRegisterEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserRegisterEventPublishProviderImpl implements UserRegisterEventPublishProvider {
    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publish(UserRegisterEvent event) {
        rabbitTemplate.convertAndSend("user.exchange.register", "user.queue.register", event);
    }
}
