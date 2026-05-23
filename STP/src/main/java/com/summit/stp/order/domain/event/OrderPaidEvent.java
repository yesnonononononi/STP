package com.summit.stp.order.domain.event;

import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;

@Builder
@Getter
public class OrderPaidEvent {
    private long orderId;
    private long creatorId;
    private String payType;
    private Timestamp payTime;
    private long packageId;
    private int quantity;
}
