package com.summit.stp.shared.service.subcribe;

class DuplicateEventException extends RuntimeException {
    DuplicateEventException(String message) {
        super(message);
    }
}
