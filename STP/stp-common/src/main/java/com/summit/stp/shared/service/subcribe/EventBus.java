package com.summit.stp.shared.service.subcribe;

import com.summit.stp.shared.exception.ParameterException;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
class EventBus implements EventManager {
    Map<Class<?>, List<EventListener<?>>> listeners = new ConcurrentHashMap<>();
    ExecutorService threadPoolExecutor;
    private final boolean async;
    private static volatile EventManager instance;

    /**
     * 单例模式
     * @param expectAsync 期盼异步?
     * @param executorService 线程池
     * @return EventManager
     */
    static EventManager getInstance(@Nullable Boolean expectAsync, @Nullable ExecutorService executorService){
        if(instance == null){
            synchronized (EventBus.class){
                if(instance == null){
                    instance = (expectAsync == null) ? new EventBus() :((executorService == null) ? new EventBus(expectAsync) : new EventBus(expectAsync, executorService));
                }else if(expectAsync != null){
                    log.warn("【EventBus】The EventBus instance has been initialized so settings failed to take effect!");
                }
            }
        }
        return instance;
    }

    private EventBus() {
        this(false, null);
    }

    private EventBus(boolean async, ExecutorService threadPoolExecutor){
        this.async = async;
        this.threadPoolExecutor = threadPoolExecutor;
    }

    private EventBus(boolean async) {
        this(async, async ? Executors.newCachedThreadPool() : null);
    }

    @Override
    public <T>boolean subscribe(Class<T> event, EventListener<T> eventListener) {
        return listeners.computeIfAbsent(event, e -> new CopyOnWriteArrayList<>()).add(eventListener);
    }

    @Override
    public <T>boolean unsubscribe(T event, EventListener<T> eventListener) {
        List<EventListener<?>> eventListeners = listeners.get(event.getClass());
        if (eventListeners == null || eventListeners.isEmpty()) return false;
        return eventListeners.remove(eventListener);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void publish(Object event) {
        if (event != null && !listeners.isEmpty()) {
            List<EventListener<?>> eventListeners = listeners.get(event.getClass());

            if (eventListeners == null || eventListeners.isEmpty()) return;
            if (async) {
                for (EventListener<?> eventListener : eventListeners) {
                    try {
                        threadPoolExecutor.submit(() -> ((EventListener<Object>)eventListener).onEvent(event, this));
                    } catch (Exception e) {
                        log.error("【异步事件发布】事件: {} 处理异常", event, e);
                    }
                }
            } else {
                for (EventListener<?> eventListener : eventListeners) {
                    try {
                        ((EventListener<Object>)eventListener).onEvent(event, this);
                    } catch (Exception e) {
                        log.error("【事件发布】事件: {} 处理异常", event, e);
                        throw e;
                    }
                }
            }
        }
    }



    @Override
    public <T> void registerEvent(Class<T> event) {
        if(event == null)throw new ParameterException("event can not be null");
        if(this.listeners.containsKey(event))throw new DuplicateEventException("event has been registered");
        this.listeners.put(event, new CopyOnWriteArrayList<>());
    }
}
