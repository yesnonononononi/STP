package com.summit.stp.admin.application.service;

import com.summit.stp.admin.application.vo.DashboardContentStatsVO;
import com.summit.stp.admin.application.vo.DashboardMemberStatsVO;
import com.summit.stp.admin.application.vo.DashboardOverviewVO;
import com.summit.stp.admin.application.vo.DashboardSystemStatusVO;
import com.summit.stp.admin.application.vo.DashboardTrendsVO;
import com.summit.stp.admin.application.vo.SystemActivityVO;

import java.util.List;

/**
 * 后台大盘数据查询与远程编排应用服务接口
 * 遵循 DDD 架构规范：由 Application Service 负责跨聚合/跨微服务数据编排
 */
public interface DashboardQueryAppService {

    DashboardOverviewVO getOverviewData();

    DashboardTrendsVO getTrendsData(String period);

    DashboardMemberStatsVO getMemberStatsData(String tab);

    DashboardContentStatsVO getContentStatsData(String period);

    DashboardSystemStatusVO getSystemStatusData();

    List<SystemActivityVO> getRecentActivities(Integer limit);
}
