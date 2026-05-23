package com.summit.stp.shared.service.subcribe.api;

import com.summit.stp.shared.service.subcribe.domain.event.EventBus;
import com.summit.stp.shared.service.subcribe.domain.event.EventListener;
import com.summit.stp.shared.service.subcribe.domain.manager.EventManager;

public class EventRegister {
    private static final EventManager eventManager = EventBus.getInstance(false,null);

    public static <T> void register(Class<T> event){
        eventManager.registerEvent(event);
    }

    public static <T> void subscribe(Class<T> event, EventListener<T> eventListener){
        eventManager.subscribe(event, eventListener);
    }
}
