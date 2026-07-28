package com.summit.stp.message.application.service;

import com.summit.stp.common.infrastructure.websocket.Event;
import jakarta.annotation.Nullable;

import java.util.function.Consumer;

public interface MessageSender {
     <T>void send(T messageVO, Event event, Long receiverId,@Nullable Consumer<Object> callBack);

}
