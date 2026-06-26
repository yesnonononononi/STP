package com.summit.stp.message.domain.model;

import lombok.Getter;

@Getter
public enum EventName {
    NORMAL_MESSAGE("normal_message"),
    WITHDRAWN_MESSAGE("withdrawn_message"),
    READ_MESSAGE("read_message");
    private final String name;

    EventName(String name) {
        this.name = name;
    }

}
