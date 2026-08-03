package com.summit.stp.coupon.domain.exception;

import com.summit.stp.common.application.domain.exception.BusinessException;

public class NoSuchCouponException extends BusinessException {
    public NoSuchCouponException(String message) {
        super(message);
    }
}
