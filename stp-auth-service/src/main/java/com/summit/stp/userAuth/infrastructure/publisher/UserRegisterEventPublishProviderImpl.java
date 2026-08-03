package com.summit.stp.userAuth.infrastructure.publisher;

import com.summit.stp.common.application.domain.event.UserRegisterEvent;
import com.summit.stp.common.application.service.queue.QueueSender;
import com.summit.stp.common.constants.MqConstants;
import com.summit.stp.userAuth.application.service.UserRegisterEventPublishProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserRegisterEventPublishProviderImpl implements UserRegisterEventPublishProvider {
    private final QueueSender queueSender;

    @Override
    public void publish(UserRegisterEvent event) {
        queueSender.send(MqConstants.User.EXCHANGE, MqConstants.User.ROUTING_KEY_REGISTER, event);
    }
}
