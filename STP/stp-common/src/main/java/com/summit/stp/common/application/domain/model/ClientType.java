package com.summit.stp.common.application.domain.model;

import lombok.Getter;

@Getter
public enum ClientType {
    ANDROID("android",1),
    IOS("ios",2),
    WEB("web",3),
    UNKNOWN("unknown",0);
    private final String name;
    private final int code;
    ClientType(String name, int code) {
        this.name = name;
        this.code = code;
    }
    public static ClientType getClientType(String name) {
        for (ClientType clientType : values()) {
            if (clientType.name.equals(name)) {
                return clientType;
            }
        }
        return UNKNOWN;
    }

    public static ClientType getClientType(int code) {
        for (ClientType clientType : values()) {
            if (clientType.code == code) {
                return clientType;
            }
        }
        return UNKNOWN;
    }
}
