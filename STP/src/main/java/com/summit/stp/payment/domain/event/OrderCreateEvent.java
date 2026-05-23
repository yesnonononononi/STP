package com.summit.stp.payment.domain.event;

import com.summit.stp.payment.domain.model.PayType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;
@Getter
@RequiredArgsConstructor
public class OrderCreateEvent {
    private final Long orderId;
    private final PayType payType;
    private final Long packageId;
    private final Integer quantity;
    private final BigDecimal amount;
    private final Long creatorId;
    private final Timestamp timestamp;


}
