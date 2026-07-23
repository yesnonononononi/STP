package com.summit.stp.userAuth.domain.exception;

import com.summit.stp.shared.exception.BusinessException;

public class RefreshTokenNoValidException extends BusinessException {
    public RefreshTokenNoValidException() {
        super("刷新令牌无效");
    }
}
