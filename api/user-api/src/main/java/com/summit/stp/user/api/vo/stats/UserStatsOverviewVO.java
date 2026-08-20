package com.summit.stp.user.api.vo.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsOverviewVO implements Serializable {
    private Long totalUsers;
    private Long todayNewUsers;
    private String userGrowthRate;
    private Long activeMembers;
    private Long todayNewMembers;
    private String memberGrowthRate;
    private Long pendingReports;
    private Long todayProcessedReports;
}
