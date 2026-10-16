package com.summit.stp.admin.application.service;

import com.summit.stp.admin.application.assembler.DashboardDataAssembler;
import com.summit.stp.admin.application.vo.DashboardContentStatsVO;
import com.summit.stp.admin.application.vo.DashboardMemberStatsVO;
import com.summit.stp.admin.application.vo.DashboardOverviewVO;
import com.summit.stp.admin.application.vo.DashboardSystemStatusVO;
import com.summit.stp.admin.application.vo.DashboardTrendsVO;
import com.summit.stp.admin.application.vo.SystemActivityVO;
import com.summit.stp.admin.domain.model.SystemActivity;
import com.summit.stp.admin.domain.model.SystemActivityRepository;
import com.summit.stp.admin.infrastructure.cache.DashboardCacheProvider;
import com.summit.stp.admin.infrastructure.metrics.GatewayMetricsProvider;
import com.summit.stp.admin.infrastructure.metrics.MqBacklogProvider;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.order.api.client.OrderFeignClient;
import com.summit.stp.order.api.vo.stats.OrderStatsOverviewVO;
import com.summit.stp.order.api.vo.stats.OrderTrendsVO;
import com.summit.stp.post.api.client.PostFeignClient;
import com.summit.stp.post.api.vo.stats.PostContentStatsVO;
import com.summit.stp.user.api.client.AdminFeignClient;
import com.summit.stp.user.api.vo.stats.MemberDistributionVO;
import com.summit.stp.user.api.vo.stats.UserStatsOverviewVO;
import com.summit.stp.user.api.vo.stats.UserTrendsVO;

import com.sun.management.OperatingSystemMXBean;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * 后台大盘数据查询与远程编排应用服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardQueryAppServiceImpl implements DashboardQueryAppService {

    /**
     * 远程依赖并发采集的统一等待上限（秒）
     */
    private static final long REMOTE_WAIT_SECONDS = 3L;

    private final AdminFeignClient adminFeignClient;
    private final PostFeignClient postFeignClient;
    private final OrderFeignClient orderFeignClient;
    private final DiscoveryClient discoveryClient;
    private final DashboardDataAssembler dashboardDataAssembler;
    private final SystemActivityRepository<SystemActivity> systemActivityRepository;
    private final DashboardCacheProvider dashboardCacheProvider;
    private final GatewayMetricsProvider gatewayMetricsProvider;
    private final MqBacklogProvider mqBacklogProvider;

    /**
     * Dashboard 专属并发线程池。
     *
     * <p>拒绝策略使用 {@link ThreadPoolExecutor.AbortPolicy}：原先的 CallerRunsPolicy 会把任务回退到
     * Tomcat 请求线程执行，导致请求线程被远程调用阻塞、进而拖垮整个 Web 容器；
     * 改为 AbortPolicy 后由 {@link #supplyAsync} 捕获 {@link RejectedExecutionException} 并降级返回 null。</p>
     */
    private final ExecutorService executorService = new ThreadPoolExecutor(
            8, 16, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(200),
            new ThreadFactory() {
                private final AtomicInteger count = new AtomicInteger(1);
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r, "dashboard-async-pool-" + count.getAndIncrement());
                    thread.setDaemon(true);
                    return thread;
                }
            },
            new ThreadPoolExecutor.AbortPolicy()
    );

    @PreDestroy
    public void shutdown() {
        log.info("【后台大盘】[应用服务] 正在优雅关闭 Dashboard 专属并发线程池");
        executorService.shutdown();
    }

    @Override
    public DashboardOverviewVO getOverviewData() {
        log.info("【后台大盘】[应用服务] 开始并发异步拉取各个微服务真实 KPI 统计数据");

        CompletableFuture<UserStatsOverviewVO> userFuture =
                fetchRemote("用户 KPI", adminFeignClient::getUserOverviewStats);
        CompletableFuture<OrderStatsOverviewVO> orderFuture =
                fetchRemote("订单 KPI", orderFeignClient::getOrderOverviewStats);

        awaitAll(REMOTE_WAIT_SECONDS, userFuture, orderFuture);

        UserStatsOverviewVO uVo = userFuture.getNow(null);
        OrderStatsOverviewVO oVo = orderFuture.getNow(null);

        return dashboardDataAssembler.assembleOverviewData(uVo, oVo);
    }

    @Override
    public DashboardTrendsVO getTrendsData(String period) {
        log.info("【后台大盘】[应用服务] 并发抓取用户与订单走势真实数据, 周期: {}", period);

        CompletableFuture<UserTrendsVO> userTrendsFuture =
                fetchRemote("用户走势", () -> adminFeignClient.getUserTrendsStats(period));
        CompletableFuture<OrderTrendsVO> orderTrendsFuture =
                fetchRemote("订单走势", () -> orderFeignClient.getOrderTrendsStats(period));

        awaitAll(REMOTE_WAIT_SECONDS, userTrendsFuture, orderTrendsFuture);

        UserTrendsVO uTrends = userTrendsFuture.getNow(null);
        OrderTrendsVO oTrends = orderTrendsFuture.getNow(null);

        return dashboardDataAssembler.assembleTrendsData(uTrends, oTrends);
    }

    @Override
    public DashboardMemberStatsVO getMemberStatsData(String tab) {
        log.info("【后台大盘】[应用服务] 并发 Fetch 真实 MemberDistributionVO, Tab: {}", tab);

        CompletableFuture<MemberDistributionVO> memberFuture =
                fetchRemote("会员分布", () -> adminFeignClient.getMemberDistributionStats(tab));

        awaitAll(REMOTE_WAIT_SECONDS, memberFuture);

        MemberDistributionVO mVo = memberFuture.getNow(null);
        return dashboardDataAssembler.assembleMemberStats(mVo);
    }

    @Override
    public DashboardContentStatsVO getContentStatsData(String period) {
        log.info("【后台大盘】[应用服务] 抓取社区内容与互动真实数据, 周期: {}", period);

        CompletableFuture<PostContentStatsVO> postStatsFuture =
                fetchRemote("社区内容统计", () -> postFeignClient.getContentStats(period));

        awaitAll(REMOTE_WAIT_SECONDS, postStatsFuture);

        PostContentStatsVO pVo = postStatsFuture.getNow(null);
        return dashboardDataAssembler.assembleContentStats(pVo);
    }

    @Override
    public DashboardSystemStatusVO getSystemStatusData() {
        log.info("【后台大盘】[应用服务] 实时采集系统与 JVM 运行指标、网关指标、MQ 积压及应用级缓存命中率");

        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        double memPercentage = maxMemory > 0 ? Math.round(usedMemory * 10.0 / maxMemory) / 10.0 : 0.0;

        double cpuUsage = 0.0;
        try {
            if (ManagementFactory.getOperatingSystemMXBean() instanceof OperatingSystemMXBean sunOsBean) {
                double load = sunOsBean.getCpuLoad();
                if (load >= 0) {
                    cpuUsage = Math.round(load * 1000.0) / 10.0;
                }
            }
        } catch (Exception e) {
            log.warn("【后台大盘】[应用服务] 采集系统 CPU 使用率异常", e);
        }

        // 应用级缓存命中率（#18）：基于本应用自增计数，不再使用 Redis 服务器全局 keyspace_hits/misses
        double appCacheHitRate = dashboardCacheProvider.getAppCacheHitRate();

        // 网关指标与 MQ 积压并发采集；取不到时返回 null（未知），前端展示 "—"，不再伪造成 0
        CompletableFuture<Double> gatewayQpsFuture =
                supplyAsync("网关 QPS", gatewayMetricsProvider::getGatewayQps);
        CompletableFuture<Double> avgRtFuture =
                supplyAsync("网关平均耗时", gatewayMetricsProvider::getAvgResponseTimeMs);
        CompletableFuture<Long> mqBacklogFuture =
                supplyAsync("MQ 积压量", mqBacklogProvider::getTotalBacklog);

        awaitAll(REMOTE_WAIT_SECONDS, gatewayQpsFuture, avgRtFuture, mqBacklogFuture);

        Double gatewayQps = gatewayQpsFuture.getNow(null);
        Double avgResponseTimeMs = avgRtFuture.getNow(null);
        Long mqBacklogCount = mqBacklogFuture.getNow(null);

        List<DashboardSystemStatusVO.ServiceHealthItem> serviceHealthItems = collectServiceHealth();

        return dashboardDataAssembler.assembleSystemStatus(
                gatewayQps, avgResponseTimeMs, mqBacklogCount,
                cpuUsage, memPercentage, appCacheHitRate, serviceHealthItems);
    }

    @Override
    public List<SystemActivityVO> getRecentActivities(Integer limit) {
        int maxLimit = (limit != null && limit > 0) ? limit : 10;
        log.info("【后台大盘】[应用服务] 查询系统最新日志预警, 条数: {}", maxLimit);
        List<SystemActivity> activities = systemActivityRepository.findRecentActivities(maxLimit);
        return dashboardDataAssembler.assembleRecentActivities(activities);
    }

    // ===================== 通用并发采集模板 (#27) =====================

    /**
     * 通用远程调用模板：并发执行 Feign 调用，失败/非成功结果统一降级为 null 并记录日志（#28）。
     */
    private <T> CompletableFuture<T> fetchRemote(String name, Supplier<Result<T>> call) {
        return supplyAsync(name, () -> {
            try {
                Result<T> res = call.get();
                if (res != null && res.isSuccess()) {
                    return res.getData();
                }
                log.warn("【后台大盘】[应用服务] 远程抓取 {} 返回失败结果: code={}", name, res == null ? "null" : res.getCode());
            } catch (Exception e) {
                log.warn("【后台大盘】[应用服务] 远程抓取 {} 数据异常, 降级返回 null", name, e);
            }
            return null;
        });
    }

    /**
     * 通用异步执行模板：线程池拒绝或执行异常时统一降级为 null，避免影响大盘主流程（#13/#28）。
     */
    private <T> CompletableFuture<T> supplyAsync(String name, Supplier<T> call) {
        try {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    return call.get();
                } catch (Exception e) {
                    log.warn("【后台大盘】[应用服务] 采集 {} 异常, 降级返回 null", name, e);
                    return null;
                }
            }, executorService);
        } catch (RejectedExecutionException e) {
            log.warn("【后台大盘】[应用服务] 线程池队列已满, 拒绝任务[{}], 降级返回 null", name, e);
            return CompletableFuture.completedFuture(null);
        }
    }

    /**
     * 统一等待并发任务，超时后取消未完成任务，避免线程被长期占用（#13）。
     */
    private void awaitAll(long timeoutSeconds, CompletableFuture<?>... futures) {
        try {
            CompletableFuture.allOf(futures).get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.warn("【后台大盘】[应用服务] 并发采集等待超时({}s), 取消未完成任务", timeoutSeconds);
            cancelUnfinished(futures);
        } catch (Exception e) {
            log.warn("【后台大盘】[应用服务] 并发采集等待异常, 取消未完成任务", e);
            cancelUnfinished(futures);
        }
    }

    private void cancelUnfinished(CompletableFuture<?>... futures) {
        for (CompletableFuture<?> f : futures) {
            if (f != null && !f.isDone()) {
                // 说明: CompletableFuture.cancel(true) 不会真正中断已在线程池中运行的任务,
                // 仅将 future 置为取消态, 使 getNow 立即返回 null。
                // 真正的远程调用超时兜底由 Feign 的 connectTimeout/readTimeout 保证（见 application.yaml）。
                f.cancel(true);
            }
        }
    }

    /**
     * 从 Nacos 动态获取微服务实例列表并生成健康度条目
     */
    private List<DashboardSystemStatusVO.ServiceHealthItem> collectServiceHealth() {
        if (discoveryClient == null) {
            return Collections.emptyList();
        }
        try {
            List<String> services = discoveryClient.getServices();
            if (services.isEmpty()) {
                return Collections.emptyList();
            }
            return services.stream().map(serviceName -> {
                List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
                int instanceCount = instances.size();
                String status = instanceCount > 0 ? "UP" : "DOWN";
                return DashboardSystemStatusVO.ServiceHealthItem.builder()
                        .name(serviceName)
                        .status(status)
                        .instances(instanceCount)
                        .uptime(status.equals("UP") ? "RUNNING" : "STOPPED")
                        .build();
            }).toList();
        } catch (Exception e) {
            log.warn("【后台大盘】[应用服务] 从 Nacos 动态获取微服务服务列表失败", e);
            return Collections.emptyList();
        }
    }
}
