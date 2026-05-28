package com.summit.stp.payment.domain.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayFailEvent {
    private Long orderId;
    private String reason;
}
