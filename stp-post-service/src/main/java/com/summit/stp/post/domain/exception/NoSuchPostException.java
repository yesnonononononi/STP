package com.summit.stp.post.domain.exception;

import com.summit.stp.common.application.domain.exception.BusinessException;

import java.io.Serializable;

public class NoSuchPostException extends BusinessException {
    public NoSuchPostException(Serializable message) {
        super(String.format("帖子不存在: %s", message));
    }
}
