package com.summit.stp.user.api.client;

import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.user.api.vo.stats.MemberDistributionVO;
import com.summit.stp.user.api.vo.stats.UserStatsOverviewVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "stp-user-service", contextId = "adminFeignClient")
public interface AdminFeignClient {

    /**
     * 判断用户是否是管理员
     *
     * @param uid 用户ID
     * @return null : 不是管理员, >= 0: 管理员等级
     */
    @GetMapping("/u/admin/internal/is/{uid}")
    Integer isAdmin(@PathVariable Long uid);

    @GetMapping("/u/admin/internal/stats/overview")
    Result<UserStatsOverviewVO> getUserOverviewStats();

    @GetMapping("/u/admin/internal/stats/member-distribution")
    Result<MemberDistributionVO> getMemberDistributionStats(@org.springframework.web.bind.annotation.RequestParam(value = "tab", required = false) String tab);

    @GetMapping("/u/admin/internal/stats/trends")
    Result<com.summit.stp.user.api.vo.stats.UserTrendsVO> getUserTrendsStats(@org.springframework.web.bind.annotation.RequestParam("period") String period);
}
