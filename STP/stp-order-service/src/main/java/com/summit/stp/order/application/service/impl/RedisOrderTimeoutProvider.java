package com.summit.stp.order.application.service.impl;

import com.summit.stp.common.util.DistributedLockUtil;
import com.summit.stp.order.application.service.OrderAppService;
import com.summit.stp.order.application.service.OrderTimeoutProvider;
import com.summit.stp.order.domain.model.Order;
import com.summit.stp.order.domain.repository.OrderRepository;
import com.summit.stp.order.infrastructure.constants.OrderConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 订单超时处理的 Redis ZSet 轮询实现。
 * Redis 负责高效调度，MySQL 的 timeout_time 作为 Redis 不可用时的最终兜底依据。
 */
@Slf4j
@Service
public class RedisOrderTimeoutProvider implements OrderTimeoutProvider {
    private final StringRedisTemplate stringRedisTemplate;
    private final OrderAppService orderAppService;
    private final OrderRepository orderRepository;
    private final DistributedLockUtil distributedLockUtil;
    private final int mysqlFallbackBatchSize;
    /** Redis 故障窗口截止时间，覆盖故障期间创建订单的整个业务 TTL。 */
    private final AtomicLong mysqlFallbackDeadline = new AtomicLong();

    public RedisOrderTimeoutProvider(
            StringRedisTemplate stringRedisTemplate,
            @Lazy OrderAppService orderAppService,
            OrderRepository orderRepository,
            DistributedLockUtil distributedLockUtil,
            @Value("${order.timeout.mysql-fallback-batch-size:" + OrderConstants.Business.MYSQL_TIMEOUT_FALLBACK_BATCH_SIZE + "}") int mysqlFallbackBatchSize) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.orderAppService = orderAppService;
        this.orderRepository = orderRepository;
        this.distributedLockUtil = distributedLockUtil;
        this.mysqlFallbackBatchSize = mysqlFallbackBatchSize;
    }

    @Override
    public void registerTimeout(Timestamp timeoutTime, Long orderId) {
        executeAfterCommit("注册订单超时任务", () -> {
            try {
                ZSetOperations<String, String> zset = stringRedisTemplate.opsForZSet();
                zset.add(OrderConstants.Cache.TIMEOUT_ZSET, String.valueOf(orderId), timeoutTime.getTime());
            } catch (Exception e) {
                markRedisUnavailable();
                log.error("【Redis超时检测】订单超时任务注册失败，订单ID: {}", orderId, e);
            }
        });
    }

    @Override
    public void cancelTimeout(Long orderId) {
        if (orderId == null) {
            return;
        }
        cancelTimeouts(List.of(orderId));
    }

    @Override
    public void cancelTimeouts(Collection<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return;
        }
        executeAfterCommit("删除订单超时任务", () -> {
            try {
                String[] members = orderIds.stream()
                        .map(String::valueOf)
                        .toArray(String[]::new);
                stringRedisTemplate.opsForZSet().remove(OrderConstants.Cache.TIMEOUT_ZSET, (Object[]) members);
            } catch (Exception e) {
                markRedisUnavailable();
                log.error("【Redis超时检测】批量删除订单超时任务失败，订单数: {}", orderIds.size(), e);
            }
        });
    }

    /**
     * 定时检查未支付超时订单，每 5 秒执行一次。Redis 不可用时，catch 分支切换到 MySQL 兜底扫描。
     */
    @Scheduled(cron = "0/5 * * * * ?")
    public void checkOrderTimeout() {
        try {
            distributedLockUtil.executeWithLock(OrderConstants.Cache.TIMEOUT_LOCK, this::processRedisTimeoutOrders);
            if (shouldRunMysqlTimeoutFallback()) {
                // Redis 恢复后继续覆盖故障窗口，避免遗漏故障期间创建但尚未到期的订单。
                processMysqlTimeoutFallback();
            }
        } catch (Exception e) {
            markRedisUnavailable();
            log.error("【订单超时】Redis 超时索引处理失败，切换 MySQL 兜底扫描", e);
            processMysqlTimeoutFallback();
        }
    }

    /**
     * Redis 超时索引处理：批量读取到期订单，再交由应用服务执行领域关单和业务补偿。
     */
    private void processRedisTimeoutOrders() {
        ZSetOperations<String, String> zset = stringRedisTemplate.opsForZSet();
        Set<String> timeoutOrderIds = zset.rangeByScore(
                OrderConstants.Cache.TIMEOUT_ZSET,
                0D,
                System.currentTimeMillis(),
                0,
                mysqlFallbackBatchSize);
        if (timeoutOrderIds == null || timeoutOrderIds.isEmpty()) {
            return;
        }

        List<Long> orderIds = timeoutOrderIds.stream()
                .map(Long::valueOf)
                .toList();
        List<Order> orders = orderRepository.findOrderByIds(orderIds);
        orderAppService.cancelOrderTimeout(orders);
        cancelTimeouts(orderIds);
    }

    /**
     * Redis 不可用或 Redis 故障恢复后，基于 payment_order.timeout_time 批量扫描兜底订单。
     *
     * 使用单次带 LIMIT 的查询，避免逐订单查询；订单关单由领域模型完成，数据库更新由仓储层批量执行。
     */
    private void processMysqlTimeoutFallback() {
        try {
            List<Order> orders = orderRepository.findPendingExpiredOrders(
                    Timestamp.from(Instant.now()),
                    mysqlFallbackBatchSize);
            if (orders.isEmpty()) {
                return;
            }
            orderAppService.cancelOrderTimeout(orders);
        } catch (Exception e) {
            log.error("【订单超时】MySQL 兜底扫描或批量关单失败", e);
        }
    }

    private void markRedisUnavailable() {
        long fallbackDeadline = System.currentTimeMillis()
                + OrderConstants.Business.TIMEOUT_MILLIS
                + OrderConstants.Business.TIMEOUT_SCAN_INTERVAL_MILLIS;
        mysqlFallbackDeadline.accumulateAndGet(fallbackDeadline, Math::max);
    }

    private boolean shouldRunMysqlTimeoutFallback() {
        return System.currentTimeMillis() <= mysqlFallbackDeadline.get();
    }

    /**
     * 将 Redis 写操作延迟到业务事务提交之后执行，且缓存失败只记录日志，不影响数据库事务结果。
     */
    private void executeAfterCommit(String operation, Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    action.run();
                } catch (Exception e) {
                    log.error("【Redis超时检测】事务提交后{}失败", operation, e);
                }
            }
        });
    }
}
