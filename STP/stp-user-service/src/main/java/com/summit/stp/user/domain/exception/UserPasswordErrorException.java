package com.summit.stp.user.domain.exception;

import com.summit.stp.common.application.domain.exception.BusinessException;

public class UserPasswordErrorException extends BusinessException {
    public UserPasswordErrorException() {
        super("密码错误", "PASSWORD_ERROR");
    }
}
