package com.summit.stp.toolbox.domain.exception;

import com.summit.stp.shared.exception.BusinessException;

public class SignInDuplicateException extends BusinessException {
    public SignInDuplicateException() {
        super("今天已经签到过了");
    }
}
