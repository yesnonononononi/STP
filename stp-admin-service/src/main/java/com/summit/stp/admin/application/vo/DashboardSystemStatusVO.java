package com.summit.stp.admin.application.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSystemStatusVO {
    private Integer gatewayQps;
    private Integer avgResponseTimeMs;
    private Double cpuUsage;
    private Double memoryUsage;
    private Double redisHitRate;
    private Integer mqBacklogCount;
    private List<ServiceHealthItem> services;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceHealthItem {
        private String name;
        private String status;
        private Integer instances;
        private String uptime;
    }
}
