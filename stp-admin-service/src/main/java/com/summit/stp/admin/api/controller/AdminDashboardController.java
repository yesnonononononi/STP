package com.summit.stp.admin.api.controller;

import com.summit.stp.admin.application.service.AdminDashboardService;
import com.summit.stp.admin.application.vo.DashboardContentStatsVO;
import com.summit.stp.admin.application.vo.DashboardMemberStatsVO;
import com.summit.stp.admin.application.vo.DashboardOverviewVO;
import com.summit.stp.admin.application.vo.DashboardSystemStatusVO;
import com.summit.stp.admin.application.vo.DashboardTrendsVO;
import com.summit.stp.admin.application.vo.SystemActivityVO;
import com.summit.stp.common.annotation.Admin;
import com.summit.stp.common.annotation.Login;
import com.summit.stp.common.application.api.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Admin
@Login
@RestController
@RequestMapping("/a/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/overview")
    public Result<DashboardOverviewVO> overview() {
        return adminDashboardService.overview();
    }

    @GetMapping("/trends")
    public Result<DashboardTrendsVO> trends(@RequestParam(value = "period", defaultValue = "30d") String period) {
        return adminDashboardService.trends(period);
    }

    @GetMapping("/member-stats")
    public Result<DashboardMemberStatsVO> memberStats(@RequestParam(value = "tab", required = false) String tab) {
        return adminDashboardService.memberStats(tab);
    }

    @GetMapping("/content-stats")
    public Result<DashboardContentStatsVO> contentStats(@RequestParam(value = "period", defaultValue = "7d") String period) {
        return adminDashboardService.contentStats(period);
    }

    @GetMapping("/system-status")
    public Result<DashboardSystemStatusVO> systemStatus() {
        return adminDashboardService.systemStatus();
    }

    @GetMapping("/recent-activities")
    public Result<List<SystemActivityVO>> recentActivities(@RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        return adminDashboardService.recentActivities(limit);
    }
}
