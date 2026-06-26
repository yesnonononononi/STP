package com.summit.stp.common.infrastructure.websocket;

import com.corundumstudio.socketio.AckCallback;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.BroadcastAckCallback;
import com.summit.stp.message.domain.model.EventName;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Event  {
    private String eventName;
    private BroadcastAckCallback<AckRequest> ackCallback;
    private EventType eventType;
}
