package com.summit.stp.user.api;

import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.user.api.vo.stats.MemberDistributionVO;
import com.summit.stp.user.api.vo.stats.UserStatsOverviewVO;
import com.summit.stp.user.api.vo.stats.UserTrendsVO;
import com.summit.stp.user.application.support.UserDashSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/u/admin/internal/stats")
@RequiredArgsConstructor
public class UserInternalStatsController {

    private final UserDashSupport userDashSupport;

    @GetMapping("/overview")
    public Result<UserStatsOverviewVO> getUserOverviewStats() {
        return Result.success(userDashSupport.getUserOverviewStats());
    }

    @GetMapping("/member-distribution")
    public Result<MemberDistributionVO> getMemberDistributionStats(@RequestParam(value = "tab", required = false) String tab) {
        return Result.success(userDashSupport.getMemberDistributionStats(tab));
    }

    @GetMapping("/trends")
    public Result<UserTrendsVO> getUserTrendsStats(@RequestParam("period") String period) {
        return Result.success(userDashSupport.getUserTrendsStats(period));
    }
}
