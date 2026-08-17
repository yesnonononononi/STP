package com.summit.stp.common.application.domain.exception;

public class DisTributeLockAcquireException extends RuntimeException{
    public DisTributeLockAcquireException(String message) {
        super(message);
    }
}
