package com.summit.stp.userAuth.domain.exception;

import com.summit.stp.common.application.domain.exception.BusinessException;

public class UserNotFoundException extends BusinessException {
    public UserNotFoundException() {
        super("用户不存在");
    }
}
