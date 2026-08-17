package com.summit.stp.common.application.domain.exception;

public class ParameterException extends BusinessException {
    public ParameterException(String message) {
        super(message, "400");
    }

    public ParameterException(String message, String errorCode) {
        super(message, errorCode);
    }
}
