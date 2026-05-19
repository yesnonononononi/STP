package com.summit.stp.payment.domain.exception;

import com.summit.stp.shared.exception.BusinessException;

public class NoSuchCommodityException extends BusinessException {
    public NoSuchCommodityException() {
        super("未找到该商品记录");
    }
}
