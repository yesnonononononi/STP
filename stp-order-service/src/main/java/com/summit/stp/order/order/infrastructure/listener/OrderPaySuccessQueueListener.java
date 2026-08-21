package com.summit.stp.order.order.infrastructure.listener;

import com.rabbitmq.client.Channel;
import com.summit.stp.common.application.domain.event.PayFailEvent;
import com.summit.stp.common.application.domain.event.PaySuccessEvent;
import com.summit.stp.common.application.domain.exception.DisTributeLockAcquireException;
import com.summit.stp.common.constants.MqConstants;
import com.summit.stp.common.util.DistributedLockUtil;
import com.summit.stp.order.order.application.service.OrderAppService;
import com.summit.stp.order.order.infrastructure.constants.OrderConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaySuccessQueueListener {
    private final OrderAppService orderAppService;
    private final StringRedisTemplate stringRedisTemplate;
    private final DistributedLockUtil distributedLockUtil;

    private final Long TIMEOUT = 3L; // 3 seconds


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MqConstants.Pay.QUEUE_SUCCESS, durable = "true",
                    arguments = {
                            @Argument(name = "x-dead-letter-exchange", value = MqConstants.Pay.EXCHANGE),
                            @Argument(name = "x-dead-letter-routing-key", value = MqConstants.Pay.ROUTING_KEY_FAIL_RECOVERER)
                    }
            ),
            exchange = @Exchange(name = MqConstants.Pay.EXCHANGE, type = "topic"),
            key = MqConstants.Pay.ROUTING_KEY_SUCCESS
    ))
    public void onOrderPaySuccess(PaySuccessEvent event, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        Long orderId = event.getOrderId();
        log.info("【MQ-order】收到支付成功事件，订单ID: {}, DeliveryTag: {}", orderId, deliveryTag);

        // 1. 获取分布式锁，防止并发回调和对账冲突
        String lockKey = OrderConstants.Cache.LOCK_PAY + orderId;

        try {
            distributedLockUtil.executeWithLock(lockKey, () -> {
                // 3. 执行核心状态流转与会员升级业务
                orderAppService.ackOrder(orderId);
            });
            // 4. 手动确认 MQ 消息
            channel.basicAck(deliveryTag, false);
            log.info("【MQ-order】订单确认成功，订单ID: {}", orderId);
        } catch (DisTributeLockAcquireException e) {
            log.info("【MQ-order】订单已提前被处理或处理中，订单ID: {}", orderId);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("【MQ-order】订单确认失败，订单ID: {}", orderId, e);
            try {
               throw e;
            } catch (IOException ioException) {
                log.error("【MQ-order】拒绝消息失败，订单ID: {}", orderId, ioException);
            }
        }
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MqConstants.Pay.QUEUE_FAIL, durable = "true",
                    arguments = {
                            @Argument(name = "x-dead-letter-exchange", value = MqConstants.Pay.EXCHANGE),
                            @Argument(name = "x-dead-letter-routing-key", value = MqConstants.Pay.ROUTING_KEY_FAIL_RECOVERER)
                    }),
            exchange = @Exchange(name = MqConstants.Pay.EXCHANGE, type = "topic"),
            key = MqConstants.Pay.ROUTING_KEY_FAIL
    ))
    public void onOrderPayFail(PayFailEvent event, Message message, Channel channel) throws IOException, InterruptedException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        Long orderId = event.getOrderId();
        log.info("【MQ-order】收到支付失败事件，订单ID: {}, DeliveryTag: {}", orderId, deliveryTag);

        String lockKey = OrderConstants.Cache.LOCK_PAY + orderId;
        RLock lock = distributedLockUtil.getLock(lockKey);
        try {
            if (lock.tryLock(5, TimeUnit.SECONDS)) {
                // 尝试原子加锁
                if (!tryConsume(orderId)) {
                    log.info("【支付失败消费】订单已在处理中或已处理，订单ID: {}", orderId);
                    channel.basicAck(deliveryTag, false);
                    return;
                }

                // 业务逻辑：取消订单并退回优惠券
                orderAppService.cancelOrder(orderId);
                channel.basicAck(deliveryTag, false);
                log.info("【支付失败消费】订单已成功标记为取消，订单ID: {}", orderId);
            } else {
                log.warn("【支付失败消费】获取分布式锁超时，消息将重回队列，订单ID: {}", orderId);
                channel.basicNack(deliveryTag, false, true);
            }
        } catch (Exception e) {
            log.error("【支付失败消费】订单取消失败，订单ID: {}", orderId, e);
            throw e;
        } finally {
            distributedLockUtil.releaseLock(lock);
        }
    }

    /**
     * 原子抢占消费标记（分布式锁 / 幂等防重）
     *
     * @return true表示第一次消费且抢占成功；false表示已被消费或正在消费中
     */
    private boolean tryConsume(Long orderId) {
        String key = OrderConstants.Cache.PAY_SUCCESS_MARK + orderId;
        // setIfAbsent 代表原子的 SETNX 语义
        Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", TIMEOUT, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }
}
