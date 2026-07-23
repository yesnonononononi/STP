package com.summit.stp.message.domain.exception;

import com.summit.stp.shared.exception.BusinessException;

public class MessageNoExistException extends BusinessException {
    public MessageNoExistException() {
        super("消息不存在");
    }
}
