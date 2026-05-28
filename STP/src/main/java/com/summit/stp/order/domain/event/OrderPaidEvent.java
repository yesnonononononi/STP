package com.summit.stp.order.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPaidEvent {
    private long orderId;
    private long creatorId;
    private String payType;
    private Timestamp payTime;
    private long packageId;
    private int quantity;
}
