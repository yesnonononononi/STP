package com.summit.stp.userAuth.domain.exception;

import com.summit.stp.shared.exception.BusinessException;

public class PasswordErrorException extends BusinessException {
    public PasswordErrorException() {
        super("密码错误", "PASSWORD_ERROR");
    }
}
