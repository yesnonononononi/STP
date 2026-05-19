package com.summit.stp.shared.subcribe.api;

import com.summit.stp.shared.subcribe.domain.event.EventBus;
import com.summit.stp.shared.subcribe.domain.manager.EventManager;

public class EventRegister {
    private static final EventManager eventManager = EventBus.getInstance(false,null);

    public static <T> void register(Class<T> event){
        eventManager.registerEvent(event);
    }


}
