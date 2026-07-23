package com.summit.stp.message.application.service;

import com.summit.stp.common.infrastructure.websocket.Event;
import com.summit.stp.shared.application.vo.MessageVO;
import com.summit.stp.shared.application.vo.SysMessageVO;
import jakarta.annotation.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

public interface MessageSender {
     <T>void send(T messageVO, Event event, Long receiverId,@Nullable Consumer<Object> callBack);

}
