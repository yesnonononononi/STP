package com.summit.stp.shared.domain.model;

import lombok.Getter;

@Getter
public enum QueueType {
    ORDER("queue.order","queue.order");
    private String exchangeName;
    private String routingKey;
    QueueType(String routingKey, String exchangeName) {
        this.routingKey = routingKey;
        this.exchangeName = exchangeName;
    }

}
