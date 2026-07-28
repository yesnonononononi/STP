package com.summit.stp.common.application.domain.exception;

public class NoFoundUserInfoException extends BusinessException{
    public NoFoundUserInfoException() {
        super("未找到用户信息");
    }
    public NoFoundUserInfoException(String message) {
        super(message);
    }
}
