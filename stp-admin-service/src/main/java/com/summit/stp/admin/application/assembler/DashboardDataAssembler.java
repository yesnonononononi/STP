package com.summit.stp.admin.application.assembler;

import com.summit.stp.admin.application.vo.DashboardContentStatsVO;
import com.summit.stp.admin.application.vo.DashboardMemberStatsVO;
import com.summit.stp.admin.application.vo.DashboardOverviewVO;
import com.summit.stp.admin.application.vo.DashboardSystemStatusVO;
import com.summit.stp.admin.application.vo.DashboardTrendsVO;
import com.summit.stp.admin.application.vo.SystemActivityVO;
import com.summit.stp.admin.domain.model.SystemActivity;
import com.summit.stp.order.api.vo.stats.OrderStatsOverviewVO;
import com.summit.stp.order.api.vo.stats.OrderTrendsVO;
import com.summit.stp.post.api.vo.stats.PostContentStatsVO;
import com.summit.stp.user.api.vo.stats.MemberDistributionVO;
import com.summit.stp.user.api.vo.stats.UserStatsOverviewVO;
import com.summit.stp.user.api.vo.stats.UserTrendsVO;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 后台大盘纯装配组件 (Pure Assembler)
 * 职责：负责将远程/领域数据转换拼装为前端 View Object，无任何网络/线程/数据库操作
 */
@Component
public class DashboardDataAssembler {

    /**
     * 组装 KPI 概览 VO
     */
    public DashboardOverviewVO assembleOverviewData(UserStatsOverviewVO uVo, OrderStatsOverviewVO oVo) {
        return DashboardOverviewVO.builder()
                .totalUsers(uVo != null && uVo.getTotalUsers() != null ? uVo.getTotalUsers().intValue() : 0)
                .todayNewUsers(uVo != null && uVo.getTodayNewUsers() != null ? uVo.getTodayNewUsers().intValue() : 0)
                .userGrowthRate(uVo != null && uVo.getUserGrowthRate() != null ? uVo.getUserGrowthRate() : "0.0%")
                .totalGmv(oVo != null && oVo.getTotalGmv() != null ? oVo.getTotalGmv() : "￥0.00")
                .todayGmv(oVo != null && oVo.getTodayGmv() != null ? oVo.getTodayGmv() : "￥0.00")
                .gmvGrowthRate(oVo != null && oVo.getGmvGrowthRate() != null ? oVo.getGmvGrowthRate() : "0.0%")
                .activeMembers(uVo != null && uVo.getActiveMembers() != null ? uVo.getActiveMembers().intValue() : 0)
                .todayNewMembers(uVo != null && uVo.getTodayNewMembers() != null ? uVo.getTodayNewMembers().intValue() : 0)
                .memberGrowthRate(uVo != null && uVo.getMemberGrowthRate() != null ? uVo.getMemberGrowthRate() : "0.0%")
                .pendingReports(uVo != null && uVo.getPendingReports() != null ? uVo.getPendingReports().intValue() : 0)
                .todayProcessedReports(uVo != null && uVo.getTodayProcessedReports() != null ? uVo.getTodayProcessedReports().intValue() : 0)
                .build();
    }

    /**
     * 组装全站趋势 VO
     */
    public DashboardTrendsVO assembleTrendsData(UserTrendsVO uTrends, OrderTrendsVO oTrends) {
        List<String> dates = (uTrends != null && uTrends.getDates() != null) ? uTrends.getDates() :
                             ((oTrends != null && oTrends.getDates() != null) ? oTrends.getDates() : Collections.emptyList());

        List<Integer> userCounts = (uTrends != null && uTrends.getNewUserCountList() != null) ? uTrends.getNewUserCountList() : Collections.emptyList();
        List<Double> gmvList = (oTrends != null && oTrends.getGmvList() != null) ? oTrends.getGmvList() : Collections.emptyList();
        List<Integer> orderCounts = (oTrends != null && oTrends.getOrderCountList() != null) ? oTrends.getOrderCountList() : Collections.emptyList();

        return DashboardTrendsVO.builder()
                .dates(dates)
                .gmvList(gmvList)
                .orderCountList(orderCounts)
                .newUserCountList(userCounts)
                .build();
    }

    /**
     * 动态组装会员分布 VO (支持多 Tab 会员套餐与 VIP 等级水平)
     */
    public DashboardMemberStatsVO assembleMemberStats(MemberDistributionVO mVo) {
        if (mVo == null) {
            return DashboardMemberStatsVO.builder()
                    .typePackageGroups(Collections.emptyList())
                    .levelDistribution(Collections.emptyList())
                    .build();
        }

        List<DashboardMemberStatsVO.TypePackageDistGroup> groups = (mVo.getTypePackageDistributions() != null) ?
                mVo.getTypePackageDistributions().stream().map(t -> DashboardMemberStatsVO.TypePackageDistGroup.builder()
                        .typeId(t.getTypeId())
                        .typeName(t.getTypeName())
                        .packages((t.getPackages() != null) ? t.getPackages().stream().map(p -> DashboardMemberStatsVO.PackageDistItem.builder()
                                .packageId(p.getPackageId())
                                .packageName(p.getPackageName())
                                .count(p.getCount())
                                .salesAmount(p.getSalesAmount())
                                .percentage(p.getPercentage())
                                .build()).toList() : Collections.emptyList())
                        .build()).toList() : Collections.emptyList();

        List<DashboardMemberStatsVO.LevelDistItem> levels = (mVo.getLevelDistributions() != null) ?
                mVo.getLevelDistributions().stream().map(l -> DashboardMemberStatsVO.LevelDistItem.builder()
                        .level(l.getLevel())
                        .levelName(l.getLevelName())
                        .count(l.getCount())
                        .percentage(l.getPercentage())
                        .build()).toList() : Collections.emptyList();

        return DashboardMemberStatsVO.builder()
                .typePackageGroups(groups)
                .levelDistribution(levels)
                .build();
    }

    /**
     * 组装社区内容趋势 VO
     */
    public DashboardContentStatsVO assembleContentStats(PostContentStatsVO pVo) {
        return DashboardContentStatsVO.builder()
                .dates(pVo != null && pVo.getDates() != null ? pVo.getDates() : Collections.emptyList())
                .postCountList(pVo != null && pVo.getPostCountList() != null ? pVo.getPostCountList() : Collections.emptyList())
                .commentCountList(pVo != null && pVo.getCommentCountList() != null ? pVo.getCommentCountList() : Collections.emptyList())
                .blockedCountList(pVo != null && pVo.getBlockedCountList() != null ? pVo.getBlockedCountList() : Collections.emptyList())
                .build();
    }

    /**
     * 组装系统运行状态 VO
     */
    public DashboardSystemStatusVO assembleSystemStatus(double cpuUsage, double memoryUsage, double redisHitRate,
                                                         List<DashboardSystemStatusVO.ServiceHealthItem> serviceHealthItems) {
        return DashboardSystemStatusVO.builder()
                .gatewayQps(0)
                .avgResponseTimeMs(0)
                .cpuUsage(cpuUsage)
                .memoryUsage(memoryUsage)
                .redisHitRate(redisHitRate)
                .mqBacklogCount(0)
                .services(serviceHealthItems)
                .build();
    }

    /**
     * 组装日志预警 VO 列表
     */
    public List<SystemActivityVO> assembleRecentActivities(List<SystemActivity> activities) {
        if (activities == null || activities.isEmpty()) {
            return Collections.emptyList();
        }
        return activities.stream().map(a -> SystemActivityVO.builder()
                .id(a.getId())
                .type(a.getType() != null ? a.getType().getCode() : "INFO")
                .typeDesc(a.getType() != null ? a.getType().getDescription() : "系统日志")
                .module(a.getModule())
                .title(a.getTitle())
                .content(a.getContent())
                .targetUrl(a.getTargetUrl())
                .createTime(a.getCreateTime())
                .build()
        ).toList();
    }
}
