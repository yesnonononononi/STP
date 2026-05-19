package com.summit.stp.shared.exception;

public class BusinessException extends RuntimeException {
    private final String errorCode;

    public BusinessException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessException(String message) {
        super(message);
        this.errorCode = "500";
    }

    public String getErrorCode() {
        return errorCode;
    }
}
