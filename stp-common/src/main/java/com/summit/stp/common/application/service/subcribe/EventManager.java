package com.summit.stp.common.application.service.subcribe;

interface EventManager {
    <T> boolean subscribe(Class<T> event, EventListener<T> eventListener);
    <T> boolean unsubscribe(T event, EventListener<T> eventListener);
    void publish(Object event);
    <T> void registerEvent(Class<T> event);
}
