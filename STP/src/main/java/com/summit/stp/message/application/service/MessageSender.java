package com.summit.stp.message.application.service;

import com.summit.stp.common.infrastructure.websocket.Event;
import com.summit.stp.message.application.vo.MessageVO;

public interface MessageSender {
    void send(MessageVO messageVO, Event event,Long receiverId);
}
