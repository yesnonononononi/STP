package com.summit.stp.shared.service.subcribe.domain.event;



@FunctionalInterface
public interface EventListener<T> {
    public void onEvent(T event,EventBus eventBus);
}
