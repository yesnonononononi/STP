package com.summit.stp.admin.application.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * associated tables: user member user-report
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverviewVO {
    private Integer totalUsers;
    private Integer todayNewUsers;
    private String userGrowthRate;
    private String totalGmv; //商品交易总额
    private String todayGmv;
    private String gmvGrowthRate;
    private Integer activeMembers;  //活跃会员数
    private Integer todayNewMembers;
    private String memberGrowthRate;
    private Integer pendingReports; //待处理风控举报
    private Integer todayProcessedReports; //今日处理的风控举报
}
