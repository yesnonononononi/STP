package com.summit.stp.payment.domain.event;

import com.summit.stp.payment.domain.model.PaySuccessType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PaySuccessEvent {
    private final Long orderId;

}
