package com.summit.stp.message.application.service.impl;

import com.corundumstudio.socketio.BroadcastAckCallback;
import com.corundumstudio.socketio.BroadcastOperations;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.summit.stp.common.infrastructure.websocket.Connector;
import com.summit.stp.common.infrastructure.websocket.Event;
import com.summit.stp.message.application.service.MessageSender;
import com.summit.stp.shared.application.vo.MessageVO;
import com.summit.stp.shared.application.vo.SysMessageVO;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Slf4j
@Component
public class MessageSenderImpl implements MessageSender {
    private final SocketIOServer socketIOServer;

    public MessageSenderImpl(SocketIOServer socketIOServer) {
        this.socketIOServer = socketIOServer;

    }

    @Override
    public <T> void send(T messageVO, Event event, Long receiverId, Consumer<Object> callback) {

        if (receiverId == null) {
            log.warn("【消息发送】接收者ID为null，跳过WebSocket实时推送。MessageVO: {}", messageVO);
            return;
        }
        BroadcastOperations roomOperations = socketIOServer.getRoomOperations(Connector.ROOM + receiverId);
        if (roomOperations.getClients().isEmpty()) {
            log.info("【消息发送】接收者ID:{} 的用户可能下线,MessageVO: {}", receiverId, messageVO);
            return;
        }
        roomOperations.sendEvent(event.getEventName(), messageVO, new BroadcastAckCallback<>(String.class) {
            @Override
            protected void onClientSuccess(SocketIOClient client, String result) {
                if (callback == null) return;
                callback.accept(result);
            }
        });
        log.info("【消息发送】已通过专属房间 user_room:{} 发送事件: {}", receiverId, event.getEventName());
    }


}
