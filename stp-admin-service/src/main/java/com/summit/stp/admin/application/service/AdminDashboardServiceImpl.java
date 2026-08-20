package com.summit.stp.admin.application.service;

import com.summit.stp.admin.application.vo.DashboardContentStatsVO;
import com.summit.stp.admin.application.vo.DashboardMemberStatsVO;
import com.summit.stp.admin.application.vo.DashboardOverviewVO;
import com.summit.stp.admin.application.vo.DashboardSystemStatusVO;
import com.summit.stp.admin.application.vo.DashboardTrendsVO;
import com.summit.stp.admin.application.vo.SystemActivityVO;
import com.summit.stp.admin.infrastructure.cache.DashboardCacheProvider;
import com.summit.stp.admin.infrastructure.constants.DashboardCacheConstants;
import com.summit.stp.common.application.api.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final DashboardQueryAppService dashboardQueryAppService;
    private final DashboardCacheProvider dashboardCacheProvider;

    @Override
    public Result<DashboardOverviewVO> overview() {
        log.info("【后台大盘】[服务层] 查询全站 KPI 概览数据");
        DashboardOverviewVO vo = dashboardCacheProvider.getOrSet(
                DashboardCacheConstants.DASHBOARD_OVERVIEW_KEY,
                DashboardOverviewVO.class,
                dashboardQueryAppService::getOverviewData
        );
        return Result.success(vo);
    }

    @Override
    public Result<DashboardTrendsVO> trends(String period) {
        String cacheKey = DashboardCacheConstants.DASHBOARD_TRENDS_KEY_PREFIX + period;
        log.info("【后台大盘】[服务层] 查询全站走势数据, 周期: {}", period);
        DashboardTrendsVO vo = dashboardCacheProvider.getOrSet(
                cacheKey,
                DashboardTrendsVO.class,
                () -> dashboardQueryAppService.getTrendsData(period)
        );
        return Result.success(vo);
    }

    @Override
    public Result<DashboardMemberStatsVO> memberStats(String tab) {
        String cacheKey = DashboardCacheConstants.DASHBOARD_MEMBER_STATS_KEY + (tab != null && !tab.isBlank() ? ":" + tab : "");
        log.info("【后台大盘】[服务层] 查询会员分布数据, Tab: {}", tab);
        DashboardMemberStatsVO vo = dashboardCacheProvider.getOrSet(
                cacheKey,
                DashboardMemberStatsVO.class,
                () -> dashboardQueryAppService.getMemberStatsData(tab)
        );
        return Result.success(vo);
    }

    @Override
    public Result<DashboardContentStatsVO> contentStats(String period) {
        String cacheKey = DashboardCacheConstants.DASHBOARD_CONTENT_STATS_KEY_PREFIX + period;
        log.info("【后台大盘】[服务层] 查询内容互动数据, 周期: {}", period);
        DashboardContentStatsVO vo = dashboardCacheProvider.getOrSet(
                cacheKey,
                DashboardContentStatsVO.class,
                () -> dashboardQueryAppService.getContentStatsData(period)
        );
        return Result.success(vo);
    }

    @Override
    public Result<DashboardSystemStatusVO> systemStatus() {
        log.info("【后台大盘】[服务层] 查询微服务健康度及实时指标");
        DashboardSystemStatusVO vo = dashboardCacheProvider.getOrSet(
                DashboardCacheConstants.DASHBOARD_SYSTEM_STATUS_KEY,
                DashboardSystemStatusVO.class,
                dashboardQueryAppService::getSystemStatusData
        );
        return Result.success(vo);
    }

    @Override
    public Result<List<SystemActivityVO>> recentActivities(Integer limit) {
        int maxLimit = (limit != null && limit > 0) ? limit : 10;
        String cacheKey = DashboardCacheConstants.DASHBOARD_RECENT_ACTIVITIES_KEY_PREFIX + maxLimit;
        log.info("【后台大盘】[服务层] 查询系统最新日志预警, 条数: {}", maxLimit);

        List<SystemActivityVO> voList = dashboardCacheProvider.getOrSetList(
                cacheKey,
                SystemActivityVO.class,
                () -> dashboardQueryAppService.getRecentActivities(maxLimit)
        );

        return Result.success(voList);
    }
}
