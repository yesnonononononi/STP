package com.summit.stp.order.domain.exception;

import com.summit.stp.shared.exception.BusinessException;

public class NoSuchMemberException extends BusinessException {
    public NoSuchMemberException() {
        super("未找到该会员记录");
    }
}
