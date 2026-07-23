package com.summit.stp.toolbox.domain.exception;

import com.summit.stp.shared.exception.BusinessException;

public class UserSignInUnInitException extends BusinessException {
    public UserSignInUnInitException(Long userId) {
        super(userId.toString());
    }
}
