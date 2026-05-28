package com.summit.stp.payment.domain.event;

import com.summit.stp.payment.domain.model.PaySuccessType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaySuccessEvent {
    private Long orderId;

}
