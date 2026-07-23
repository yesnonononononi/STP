package com.summit.stp.message.domain.exception;

import com.summit.stp.shared.exception.BusinessException;

public class SessionExistException extends BusinessException {
    public SessionExistException(String message) {
        super(message);
    }
}
