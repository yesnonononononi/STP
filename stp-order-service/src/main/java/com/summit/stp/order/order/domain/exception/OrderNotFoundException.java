package com.summit.stp.order.order.domain.exception;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message) {
        super(message);
    }
    public OrderNotFoundException() {
        super("未找到订单信息");
    }
}
