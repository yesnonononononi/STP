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
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 后台大盘数据查询与远程编排应用服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardQueryAppServiceImpl implements DashboardQueryAppService {

    private final AdminFeignClient adminFeignClient;
    private final PostFeignClient postFeignClient;
    private final OrderFeignClient orderFeignClient;
    private final DiscoveryClient discoveryClient;
    private final DashboardDataAssembler dashboardDataAssembler;
    private final SystemActivityRepository<SystemActivity> systemActivityRepository;
    private final StringRedisTemplate redisTemplate;

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
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    @PreDestroy
    public void shutdown() {
        log.info("【后台大盘】[应用服务] 正在优雅关闭 Dashboard 专属并发线程池");
        executorService.shutdown();
    }

    @Override
    public DashboardOverviewVO getOverviewData() {
        log.info("【后台大盘】[应用服务] 开始并发异步拉取各个微服务真实 KPI 统计数据");

        CompletableFuture<UserStatsOverviewVO> userFuture = CompletableFuture.supplyAsync(() -> {
            try {
                Result<UserStatsOverviewVO> res = adminFeignClient.getUserOverviewStats();
                if (res != null && res.isSuccess()) {
                    return res.getData();
                }
            } catch (Exception e) {
                log.warn("【后台大盘】[应用服务] 远程抓取用户 KPI 真实数据失败", e);
            }
            return null;
        }, executorService);

        CompletableFuture<OrderStatsOverviewVO> orderFuture = CompletableFuture.supplyAsync(() -> {
            try {
                Result<OrderStatsOverviewVO> res = orderFeignClient.getOrderOverviewStats();
                if (res != null && res.isSuccess()) {
                    return res.getData();
                }
            } catch (Exception e) {
                log.warn("【后台大盘】[应用服务] 远程抓取订单 KPI 真实数据失败", e);
            }
            return null;
        }, executorService);

        try {
            CompletableFuture.allOf(userFuture, orderFuture).get(3, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("【后台大盘】[应用服务] 并发抓取 KPI 部分等待超时", e);
        }

        UserStatsOverviewVO uVo = userFuture.getNow(null);
        OrderStatsOverviewVO oVo = orderFuture.getNow(null);

        return dashboardDataAssembler.assembleOverviewData(uVo, oVo);
    }

    @Override
    public DashboardTrendsVO getTrendsData(String period) {
        log.info("【后台大盘】[应用服务] 并发抓取用户与订单走势真实数据, 周期: {}", period);

        CompletableFuture<UserTrendsVO> userTrendsFuture = CompletableFuture.supplyAsync(() -> {
            try {
                Result<UserTrendsVO> res = adminFeignClient.getUserTrendsStats(period);
                if (res != null && res.isSuccess()) {
                    return res.getData();
                }
            } catch (Exception e) {
                log.warn("【后台大盘】[应用服务] 抓取真实用户走势异常", e);
            }
            return null;
        }, executorService);

        CompletableFuture<OrderTrendsVO> orderTrendsFuture = CompletableFuture.supplyAsync(() -> {
            try {
                Result<OrderTrendsVO> res = orderFeignClient.getOrderTrendsStats(period);
                if (res != null && res.isSuccess()) {
                    return res.getData();
                }
            } catch (Exception e) {
                log.warn("【后台大盘】[应用服务] 抓取真实订单走势异常", e);
            }
            return null;
        }, executorService);

        try {
            CompletableFuture.allOf(userTrendsFuture, orderTrendsFuture).get(3, TimeUnit.SECONDS);
        } catch (Exception ignored) {}

        UserTrendsVO uTrends = userTrendsFuture.getNow(null);
        OrderTrendsVO oTrends = orderTrendsFuture.getNow(null);

        return dashboardDataAssembler.assembleTrendsData(uTrends, oTrends);
    }

    @Override
    public DashboardMemberStatsVO getMemberStatsData(String tab) {
        log.info("【后台大盘】[应用服务] 并发 Fetch 真实 MemberDistributionVO, Tab: {}", tab);
        CompletableFuture<MemberDistributionVO> memberFuture = CompletableFuture.supplyAsync(() -> {
            try {
                Result<MemberDistributionVO> res = adminFeignClient.getMemberDistributionStats(tab);
                if (res != null && res.isSuccess()) {
                    return res.getData();
                }
            } catch (Exception e) {
                log.warn("【后台大盘】[应用服务] 抓取真实会员分布数据异常", e);
            }
            return null;
        }, executorService);

        try {
            memberFuture.get(2, TimeUnit.SECONDS);
        } catch (Exception ignored) {}

        MemberDistributionVO mVo = memberFuture.getNow(null);
        return dashboardDataAssembler.assembleMemberStats(mVo);
    }

    @Override
    public DashboardContentStatsVO getContentStatsData(String period) {
        log.info("【后台大盘】[应用服务] 抓取社区内容与互动真实数据, 周期: {}", period);
        CompletableFuture<PostContentStatsVO> postStatsFuture = CompletableFuture.supplyAsync(() -> {
            try {
                Result<PostContentStatsVO> res = postFeignClient.getContentStats(period);
                if (res != null && res.isSuccess()) {
                    return res.getData();
                }
            } catch (Exception e) {
                log.warn("【后台大盘】[应用服务] 抓取真实社区内容统计异常", e);
            }
            return null;
        }, executorService);

        try {
            postStatsFuture.get(2, TimeUnit.SECONDS);
        } catch (Exception ignored) {}

        PostContentStatsVO pVo = postStatsFuture.getNow(null);
        return dashboardDataAssembler.assembleContentStats(pVo);
    }

    @Override
    public DashboardSystemStatusVO getSystemStatusData() {
        log.info("【后台大盘】[应用服务] 实时采集系统与 JVM 运行指标及 Redis 状态");

        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        double memPercentage = maxMemory > 0 ? Math.round(usedMemory * 100.0 / maxMemory * 10.0) / 10.0 : 0.0;

        double cpuUsage = 0.0;
        try {
            java.lang.management.OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            if (osBean instanceof OperatingSystemMXBean sunOsBean) {
                double load = sunOsBean.getCpuLoad();
                if (load >= 0) {
                    cpuUsage = Math.round(load * 1000.0) / 10.0;
                }
            }
        } catch (Exception e) {
            log.warn("【后台大盘】[应用服务] 采集系统 CPU 使用率异常", e);
        }

        double redisHitRate = calculateRealRedisHitRate();

        List<DashboardSystemStatusVO.ServiceHealthItem> serviceHealthItems = Collections.emptyList();
        if (discoveryClient != null) {
            try {
                List<String> services = discoveryClient.getServices();
                if (services != null && !services.isEmpty()) {
                    serviceHealthItems = services.stream().map(serviceName -> {
                        List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
                        int instanceCount = instances != null ? instances.size() : 0;
                        String status = instanceCount > 0 ? "UP" : "DOWN";
                        return DashboardSystemStatusVO.ServiceHealthItem.builder()
                                .name(serviceName)
                                .status(status)
                                .instances(instanceCount)
                                .uptime(status.equals("UP") ? "RUNNING" : "STOPPED")
                                .build();
                    }).toList();
                }
            } catch (Exception e) {
                log.warn("【后台大盘】[应用服务] 从 Nacos 动态获取微服务服务列表失败", e);
            }
        }

        return dashboardDataAssembler.assembleSystemStatus(cpuUsage, memPercentage, redisHitRate, serviceHealthItems);
    }

    @Override
    public List<SystemActivityVO> getRecentActivities(Integer limit) {
        int maxLimit = (limit != null && limit > 0) ? limit : 10;
        log.info("【后台大盘】[应用服务] 查询系统最新日志预警, 条数: {}", maxLimit);
        List<SystemActivity> activities = systemActivityRepository.findRecentActivities(maxLimit);
        return dashboardDataAssembler.assembleRecentActivities(activities);
    }

    /**
     * 动态读取 Redis 服务器 stats 实时计算命中率
     */
    private double calculateRealRedisHitRate() {
        if (redisTemplate == null) return 0.0;
        try {
            Properties stats = redisTemplate.execute((RedisConnection connection) -> connection.serverCommands().info("stats"));
            if (stats != null) {
                String hitsStr = stats.getProperty("keyspace_hits");
                String missesStr = stats.getProperty("keyspace_misses");
                if (hitsStr != null && missesStr != null) {
                    long hits = Long.parseLong(hitsStr);
                    long misses = Long.parseLong(missesStr);
                    long total = hits + misses;
                    if (total > 0) {
                        return Math.round(hits * 100.0 / total * 10.0) / 10.0;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("【后台大盘】[应用服务] 采集 Redis 实时命中率异常", e);
        }
        return 0.0;
    }
}
