package com.summit.stp.userAuth.domain.exception;

import com.summit.stp.common.application.domain.exception.BusinessException;

public class ResetPasswordException extends BusinessException {
    public ResetPasswordException(String message) {
        super(message);
    }
}
