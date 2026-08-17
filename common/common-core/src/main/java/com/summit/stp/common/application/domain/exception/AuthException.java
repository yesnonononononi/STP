package com.summit.stp.common.application.domain.exception;

import lombok.Getter;

/**
 * 认证鉴权异常
 * - 401: 未登录或登录已过期
 * - 403: 已登录但权限不足
 */
@Getter
public class AuthException extends RuntimeException {

    private final int status;

    public AuthException(int status, String message) {
        super(message);
        this.status = status;
    }
}
