package com.summit.stp.shared.service.subcribe;

class EventRegister {
    private static final EventManager eventManager = EventBus.getInstance(false, null);

    static <T> void register(Class<T> event){
        eventManager.registerEvent(event);
    }

    static <T> void subscribe(Class<T> event, EventListener<T> eventListener){
        eventManager.subscribe(event, eventListener);
    }
}
