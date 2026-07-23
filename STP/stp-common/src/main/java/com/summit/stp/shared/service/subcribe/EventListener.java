package com.summit.stp.shared.service.subcribe;

interface EventListener<T> {
    void onEvent(T event, EventBus eventBus);
}
