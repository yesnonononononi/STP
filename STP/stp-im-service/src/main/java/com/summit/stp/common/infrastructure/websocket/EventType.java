package com.summit.stp.common.infrastructure.websocket;

import lombok.Getter;

@Getter
public enum EventType {
    SYSTEM(1),
    USER(2);
    private final int code;
    EventType(int code) {
        this.code = code;
    }
    public static EventType getEventType(int code) {
        for (EventType eventType : values()) {
            if (eventType.code == code) {
                return eventType;
            }
        }
        return null;
    }
}
