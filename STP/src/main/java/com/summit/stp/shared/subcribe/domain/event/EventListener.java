package com.summit.stp.shared.subcribe.domain.event;



@FunctionalInterface
public interface EventListener<T> {
    public void onEvent(T event,EventBus eventBus);
}
