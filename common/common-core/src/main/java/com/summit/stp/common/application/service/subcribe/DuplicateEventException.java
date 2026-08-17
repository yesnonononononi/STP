package com.summit.stp.common.application.service.subcribe;

class DuplicateEventException extends RuntimeException {
    DuplicateEventException(String message) {
        super(message);
    }
}
