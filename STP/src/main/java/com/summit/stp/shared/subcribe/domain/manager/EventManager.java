package com.summit.stp.shared.subcribe.domain.manager;

import com.summit.stp.shared.subcribe.domain.event.EventListener;


public interface EventManager {
    /**
     * 订阅事件
     */
    public <T>boolean subscribe(Class<T> event, EventListener<T> eventListener);

    /**
     * 取消订阅事件
     */
    public <T>boolean unsubscribe(T event, EventListener<T> eventListener);


    /**
     * 发布事件
     */
    public void publish(Object event) ;


    /**
     * 注册事件
     * @param event 事件类
     */
    public <T>void registerEvent(Class<T> event);
}
