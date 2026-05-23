package com.summit.stp.user.domain.exception;

import com.summit.stp.shared.exception.BusinessException;

public class UserPasswordErrorException extends BusinessException {
    public UserPasswordErrorException() {
        super("密码错误", "PASSWORD_ERROR");
    }
}
