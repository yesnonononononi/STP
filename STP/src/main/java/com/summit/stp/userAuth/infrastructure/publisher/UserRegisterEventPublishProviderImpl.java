package com.summit.stp.userAuth.infrastructure.publisher;

import com.summit.stp.user.application.service.UserMessageSender;
import com.summit.stp.userAuth.application.service.UserRegisterEventPublishProvider;
import com.summit.stp.userAuth.domain.event.UserRegisterEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserRegisterEventPublishProviderImpl implements UserRegisterEventPublishProvider {
    private final UserMessageSender userMessageSender;

    @Override
    public void publish(UserRegisterEvent event) {
        userMessageSender.sendUserRegister(event);
    }
}
