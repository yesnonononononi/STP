package com.summit.stp.message.infrastructure.websocket;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.BroadcastAckCallback;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Event  {
    private String eventName;
    private BroadcastAckCallback<AckRequest> ackCallback;
    private EventType eventType;
}
