package com.summit.stp.payment.domain.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PayFailEvent {
    private final Long orderId;
    private final String reason;
}
