package com.summit.stp.order.order.domain.model.stats;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderOverviewStat {
    private Double totalGmv;
    private Double todayGmv;
    private String gmvGrowthRate;
}
