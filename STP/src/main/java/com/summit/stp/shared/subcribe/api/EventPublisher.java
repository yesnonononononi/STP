package com.summit.stp.shared.subcribe.api;


import com.summit.stp.shared.subcribe.domain.event.EventBus;
import com.summit.stp.shared.subcribe.domain.event.EventListener;
import com.summit.stp.shared.subcribe.domain.exception.EventPublishException;
import com.summit.stp.shared.subcribe.domain.manager.EventManager;



public class EventPublisher {
    private static final EventManager eventManager = EventBus.getInstance(false,null);

    private EventPublisher() {
    }
    /**
     * 发布事件
     * @param event
     */
    public static void publish(Object event, EventListener<?> eventListener){
        try {
            eventManager.publish(event);
        }catch (Exception e){
            throw new EventPublishException(String.format("Event:%s publish happened a exception %s",event,e));
        }
    }
}
