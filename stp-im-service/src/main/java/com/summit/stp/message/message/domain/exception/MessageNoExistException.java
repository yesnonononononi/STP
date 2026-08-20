package com.summit.stp.message.message.domain.exception;

import com.summit.stp.common.application.domain.exception.BusinessException;

public class MessageNoExistException extends BusinessException {
    public MessageNoExistException() {
        super("消息不存在");
    }
}
