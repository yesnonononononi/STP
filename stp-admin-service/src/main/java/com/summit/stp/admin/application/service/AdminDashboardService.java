package com.summit.stp.admin.application.service;

import com.summit.stp.admin.application.vo.*;
import com.summit.stp.common.application.api.result.Result;

import com.summit.stp.admin.application.vo.DashboardOverviewVO;
import java.util.List;

public interface AdminDashboardService {

    Result<DashboardOverviewVO> overview();

    Result<DashboardTrendsVO> trends(String period);

    Result<DashboardMemberStatsVO> memberStats(String tab);

    Result<DashboardContentStatsVO> contentStats(String period);

    Result<DashboardSystemStatusVO> systemStatus();

    Result<List<SystemActivityVO>> recentActivities(Integer limit);
}
