package com.summit.stp.order.domain.exception;

import com.summit.stp.common.application.domain.exception.BusinessException;

public class OrderDuplicationException extends BusinessException {
    public OrderDuplicationException(String message) {
        super(message);
    }
}
