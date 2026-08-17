package com.summit.stp.common.application.domain.exception;

public class UnAuthorizedException extends BusinessException {
    public UnAuthorizedException(String message) {
        super(message);
    }
}
