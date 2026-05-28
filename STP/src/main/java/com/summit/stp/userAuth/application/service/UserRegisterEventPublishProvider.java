package com.summit.stp.userAuth.application.service;

import com.summit.stp.userAuth.domain.event.UserRegisterEvent;

public interface UserRegisterEventPublishProvider {
    void publish(UserRegisterEvent event);
}
