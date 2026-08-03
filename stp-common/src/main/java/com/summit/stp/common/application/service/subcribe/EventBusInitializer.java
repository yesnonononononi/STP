package com.summit.stp.common.application.service.subcribe;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;

import java.util.Map;

@Slf4j
//@Component
@RequiredArgsConstructor
class EventBusInitializer implements SmartInitializingSingleton {

    private final ApplicationContext applicationContext;

    @SuppressWarnings("unchecked")
    @Override
    public void afterSingletonsInstantiated() {
        log.info("【EventBus】开始自动注册领域事件监听器...");
        
        // 获取 Spring 容器中所有实现了 EventListener 接口的 Bean
        Map<String, EventListener> listeners = applicationContext.getBeansOfType(EventListener.class);
        
        listeners.values().forEach(listener -> {
            // 使用 ResolvableType 解析监听器实现的具体泛型事件类型 T
            ResolvableType resolvableType = ResolvableType.forClass(listener.getClass()).as(EventListener.class);
            Class<?> eventClass = resolvableType.getGeneric(0).resolve();
            
            if (eventClass != null) {
                // 注册事件与订阅监听器
                EventRegister.register(eventClass);
                EventRegister.subscribe((Class<Object>) eventClass, (EventListener<Object>) listener);
                log.info("【EventBus】成功自动订阅监听器: {} -> 监听事件: {}", 
                        listener.getClass().getSimpleName(), eventClass.getSimpleName());
            } else {
                log.warn("【EventBus】监听器: {} 未指定具体的事件泛型，无法自动订阅", listener.getClass().getName());
            }
        });
        
        log.info("【EventBus】领域事件监听器自动注册完成！");
    }
}
