package com.summit.stp.order.application.service;

/**
 * 订单超时延迟处理提供器接口 (防腐层)
 * 隔离底层的超时机制实现（如 Redis ZSet 轮询、RabbitMQ TTL 延时队列、RocketMQ 延时消息等）
 */
public interface OrderTimeoutProvider {
    /**
     * 注册订单超时延迟任务
     * @param duration 延迟毫秒数
     * @param orderId 订单ID
     */
    void registerTimeout(Long duration, Long orderId);

    /**
     * 撤销订单超时延迟任务
     * @param orderId 订单ID
     */
    void cancelTimeout(Long orderId);
}
