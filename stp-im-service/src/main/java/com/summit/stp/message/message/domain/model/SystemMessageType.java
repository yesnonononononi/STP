package com.summit.stp.message.message.domain.model;

import lombok.Getter;

@Getter
public enum SystemMessageType {
    BROADCAST(1, "广播消息"),
    PERSONAL(2, "私有消息");

    private final int code;
    private final String desc;

    SystemMessageType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static SystemMessageType fromCode(Integer code) {
        if (code == null) {
            return BROADCAST;
        }
        for (SystemMessageType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return BROADCAST;
    }
}
