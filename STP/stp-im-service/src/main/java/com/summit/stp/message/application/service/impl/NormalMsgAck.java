package com.summit.stp.message.application.service.impl;



import com.corundumstudio.socketio.BroadcastAckCallback;
import com.corundumstudio.socketio.SocketIOClient;
import com.summit.stp.message.api.dto.request.AckRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NormalMsgAck extends BroadcastAckCallback<AckRequest> {
    public NormalMsgAck() {
        super(AckRequest.class);
    }

    @Override
    protected void onClientSuccess(SocketIOClient client, AckRequest result) {
        log.info("【SocketIO】消息:{}已被接收,会话:{}",result.getMessageId(),result.getSessionId());
    }
}
