package com.summit.stp.common.application.domain.exception;

/**
 * 未找到会员记录异常 - 跨服务共享的异常
 */
public class NoSuchMemberException extends BusinessException {
    public NoSuchMemberException() {
        super("未找到该会员记录");
    }
}
