package com.summit.stp.shared.exception;

public class NoFoundUserInfoException extends BusinessException{
    public NoFoundUserInfoException() {
        super("未找到用户信息");
    }
}
