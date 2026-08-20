package com.summit.stp.order.api.vo.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatsOverviewVO implements Serializable {
    private String totalGmv;
    private String todayGmv;
    private String gmvGrowthRate;
}
