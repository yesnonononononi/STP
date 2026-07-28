package com.summit.stp.order.application.service;

import java.sql.Timestamp;
import java.util.Collection;

/**
 * 订单超时延迟处理提供器接口 (防腐层)
 * 隔离底层的超时机制实现（如 Redis ZSet 轮询、RabbitMQ TTL 延时队列、RocketMQ 延时消息等）。
 */
public interface OrderTimeoutProvider {
    /**
     * 注册订单超时延迟任务。
     *
     * @param timeoutTime 订单超时绝对时间快照
     * @param orderId     订单ID
     */
    void registerTimeout(Timestamp timeoutTime, Long orderId);

    /**
     * 撤销单个订单超时延迟任务。
     *
     * @param orderId 订单ID
     */
    void cancelTimeout(Long orderId);

    /**
     * 批量撤销订单超时延迟任务。
     *
     * @param orderIds 订单ID集合
     */
    void cancelTimeouts(Collection<Long> orderIds);
}
