package com.summit.stp.message.application.service.impl;

import com.corundumstudio.socketio.BroadcastOperations;
import com.corundumstudio.socketio.SocketIOServer;
import com.summit.stp.common.infrastructure.websocket.Connector;
import com.summit.stp.common.infrastructure.websocket.Event;
import com.summit.stp.message.application.service.MessageSender;
import com.summit.stp.message.application.vo.MessageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MessageSenderImpl implements MessageSender {
    private final SocketIOServer socketIOServer;

    public MessageSenderImpl(SocketIOServer socketIOServer) {
        this.socketIOServer = socketIOServer;

    }

    @Override
    public void send(MessageVO messageVO, Event event,Long receiverId) {
        if (receiverId == null) {
            log.warn("【消息发送】接收者ID为null，跳过WebSocket实时推送。MessageVO: {}", messageVO);
            return;
        }
        BroadcastOperations roomOperations = socketIOServer.getRoomOperations(Connector.ROOM + receiverId);
        if (roomOperations.getClients().isEmpty()) {
            log.info("【消息发送】接收者ID:{} 的用户已下线，跳过WebSocket实时推送。MessageVO: {}", receiverId, messageVO);
            return;
        }
        roomOperations.sendEvent(event.getEventName(), messageVO );
        log.info("【消息发送】已通过专属房间 user_room:{} 发送事件: {}", receiverId, event.getEventName());
    }
}
