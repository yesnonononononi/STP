package com.summit.stp.common.application.service.subcribe;

interface EventListener<T> {
    void onEvent(T event, EventBus eventBus);
}
