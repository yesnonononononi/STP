package com.summit.stp.shared.service.subcribe;

class EventPublisher {
    private static final EventManager eventManager = EventBus.getInstance(false, null);

    private EventPublisher() {
    }
    /**
     * 发布事件
     * @param event
     */
    static void publish(Object event){
        try {
            eventManager.publish(event);
        }catch (Exception e){
            throw new EventPublishException(String.format("Event:%s publish happened a exception %s",event,e));
        }
    }
}
