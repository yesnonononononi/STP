package com.summit.stp.payment.domain.exception;

public class NoSuchStrategyException extends RuntimeException {
    public NoSuchStrategyException(String message) {
        super(message);
    }
}
