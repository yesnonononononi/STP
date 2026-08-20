package com.summit.stp.user.application.support;

import com.summit.stp.member.domain.model.Member;
import com.summit.stp.member.domain.model.MemberLevelConfig;
import com.summit.stp.member.domain.model.MemberType;
import com.summit.stp.member.domain.repository.MemberLevelConfigRepository;
import com.summit.stp.member.domain.repository.MemberRepository;
import com.summit.stp.member.domain.repository.UserMemberRepository;
import com.summit.stp.user.api.vo.stats.MemberDistributionVO;
import com.summit.stp.user.api.vo.stats.UserStatsOverviewVO;
import com.summit.stp.user.api.vo.stats.UserTrendsVO;
import com.summit.stp.user.domain.model.User;
import com.summit.stp.user.domain.model.UserReport;
import com.summit.stp.user.domain.model.UserReportRepository;
import com.summit.stp.user.domain.model.stats.UserTrendStat;
import com.summit.stp.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 用户模块 Dashboard 数据查询底层支持组件
 * 严格遵循 DDD 架构规范：基于 UserRepository / UserReportRepository / UserMemberRepository 领域仓储查询
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserDashSupport {

    private final UserRepository<User> userRepository;
    private final UserReportRepository<UserReport> userReportRepository;
    private final UserMemberRepository userMemberRepository;
    private final MemberRepository<Member> memberRepository;
    private final MemberLevelConfigRepository<MemberLevelConfig> memberLevelConfigRepository;

    /**
     * 查询用户概览真实统计数据（从数据库实时聚合）
     */
    public UserStatsOverviewVO getUserOverviewStats() {
        log.info("【用户模块】[DashSupport] 基于领域仓储查询用户大盘真实数据");
        LocalDateTime startOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        Instant startOfTodayInstant = startOfToday.atZone(ZoneId.systemDefault()).toInstant();
        Instant startOfYesterdayInstant = startOfToday.minusDays(1).atZone(ZoneId.systemDefault()).toInstant();

        // 1. 真实数据库总用户数与新增用户数
        long totalUsers = userRepository.countTotalUsers();
        long todayNewUsers = userRepository.countUsersCreatedAfter(startOfTodayInstant);
        long afterYesterdayNew = userRepository.countUsersCreatedAfter(startOfYesterdayInstant);
        long yesterdayNewUsers = afterYesterdayNew - todayNewUsers;

        String userGrowthRate = (yesterdayNewUsers > 0)
                ? String.format("%.1f%%", (todayNewUsers - yesterdayNewUsers) * 100.0 / yesterdayNewUsers)
                : (todayNewUsers > 0 ? "+100.0%" : "0.0%");

        // 2. 真实数据库风控举报数
        long pendingReports = userReportRepository.countPendingReports();
        long todayProcessedReports = userReportRepository.countProcessedReportsAfter(startOfToday);

        // 3. 真实数据库会员数据聚合
        long activeMembers = userMemberRepository.countActiveMembers();
        long todayNewMembers = userMemberRepository.countNewMembersAfter(startOfTodayInstant);
        long afterYesterdayNewMembers = userMemberRepository.countNewMembersAfter(startOfYesterdayInstant);
        long yesterdayNewMembers = afterYesterdayNewMembers - todayNewMembers;

        String memberGrowthRate = (yesterdayNewMembers > 0)
                ? String.format("%.1f%%", (todayNewMembers - yesterdayNewMembers) * 100.0 / yesterdayNewMembers)
                : (todayNewMembers > 0 ? "+100.0%" : "0.0%");

        return UserStatsOverviewVO.builder()
                .totalUsers(totalUsers)
                .todayNewUsers(todayNewUsers)
                .userGrowthRate(userGrowthRate)
                .activeMembers(activeMembers)
                .todayNewMembers(todayNewMembers)
                .memberGrowthRate(memberGrowthRate)
                .pendingReports(pendingReports)
                .todayProcessedReports(todayProcessedReports)
                .build();
    }

    /**
     * 查询会员等级与套餐分布真实统计数据 (支持按 Tab 筛选)
     */
    public MemberDistributionVO getMemberDistributionStats(String tab) {
        log.info("【用户模块】[DashSupport] 基于领域仓储动态查询会员套餐与VIP等级分布真实统计, Tab: {}", tab);

        // 1. 动态构建 VIP 等级水平占比 (Tab 3: 等级水平)
        List<MemberLevelConfig> configs = memberLevelConfigRepository.findAll();
        Map<Long, Long> levelCounts = userMemberRepository.countMemberLevelDistribution();

        long totalUsers = userRepository.countTotalUsers();
        long totalVipUsers = levelCounts.entrySet().stream()
                .filter(e -> e.getKey() != null && e.getKey() > 0)
                .mapToLong(Map.Entry::getValue)
                .sum();

        long nonVipCount = Math.max(0L, totalUsers - totalVipUsers);
        double nonVipPct = totalUsers > 0 ? Math.round(nonVipCount * 100.0 / totalUsers * 10.0) / 10.0 : 0.0;

        List<MemberDistributionVO.MemberLevelDist> levelDists = new ArrayList<>();
        levelDists.add(MemberDistributionVO.MemberLevelDist.builder()
                .level(0)
                .levelName("非VIP用户")
                .count(nonVipCount)
                .percentage(nonVipPct)
                .build());

        if (configs != null) {
            for (MemberLevelConfig c : configs) {
                if (c == null || c.getLevel() == null) continue;
                long cnt = levelCounts.getOrDefault(c.getLevel(), 0L);
                double pct = totalUsers > 0 ? Math.round(cnt * 100.0 / totalUsers * 10.0) / 10.0 : 0.0;
                levelDists.add(MemberDistributionVO.MemberLevelDist.builder()
                        .level(c.getLevel().intValue())
                        .levelName(c.getLevelName() != null ? c.getLevelName() : "VIP " + c.getLevel())
                        .count(cnt)
                        .percentage(pct)
                        .build());
            }
        }

        // 2. 动态构建 MemberType 下的套餐占比 (Tab 1: 普通会员, Tab 2: 超级会员)
        Map<Long, Long> packageUserCounts = userMemberRepository.countPackageUserDistribution();
        List<Member> allPackages = memberRepository.list();

        List<MemberDistributionVO.MemberTypePackageDist> typePackageDists = new ArrayList<>();
        for (MemberType type : MemberType.values()) {
            if (type == null) continue;
            // 如果指定了 tab 筛选，只保留匹配的 type
            if ("type_1".equalsIgnoreCase(tab) || "1".equals(tab) || "regular".equalsIgnoreCase(tab)) {
                if (!Long.valueOf(1L).equals(type.getTypeId())) continue;
            } else if ("type_2".equalsIgnoreCase(tab) || "2".equals(tab) || "super".equalsIgnoreCase(tab)) {
                if (!Long.valueOf(2L).equals(type.getTypeId())) continue;
            }

            List<Member> typePkgs = (allPackages != null) ? allPackages.stream()
                    .filter(p -> p != null && p.getTypeId() != null && p.getTypeId().equals(type.getTypeId()))
                    .toList() : Collections.emptyList();

            long totalTypeUsers = typePkgs.stream()
                    .mapToLong(p -> packageUserCounts.getOrDefault(p.getId(), 0L))
                    .sum();

            List<MemberDistributionVO.PackageItem> packageItems = new ArrayList<>();
            for (Member pkg : typePkgs) {
                long count = packageUserCounts.getOrDefault(pkg.getId(), 0L);
                double pct = totalTypeUsers > 0 ? Math.round(count * 100.0 / totalTypeUsers * 10.0) / 10.0 : 0.0;
                double priceVal = (pkg.getPrice() != null) ? pkg.getPrice().doubleValue() : 0.0;
                double sales = priceVal * count;
                packageItems.add(MemberDistributionVO.PackageItem.builder()
                        .packageId(pkg.getId())
                        .packageName(pkg.getName())
                        .count(count)
                        .salesAmount(sales)
                        .percentage(pct)
                        .build());
            }

            typePackageDists.add(MemberDistributionVO.MemberTypePackageDist.builder()
                    .typeId(type.getTypeId())
                    .typeName(type.getTypeName())
                    .packages(packageItems)
                    .build());
        }

        long goldCount = levelCounts.getOrDefault(1L, 0L);
        long silverCount = levelCounts.getOrDefault(2L, 0L);
        long diamondCount = levelCounts.entrySet().stream()
                .filter(e -> e.getKey() != null && e.getKey() >= 3L)
                .mapToLong(Map.Entry::getValue)
                .sum();

        return MemberDistributionVO.builder()
                .typePackageDistributions(typePackageDists)
                .levelDistributions(levelDists)
                .normalCount(nonVipCount)
                .goldCount(goldCount)
                .silverCount(silverCount)
                .diamondCount(diamondCount)
                .build();
    }

    /**
     * 查询用户增长走势真实统计数据
     */
    public UserTrendsVO getUserTrendsStats(String period) {
        log.info("【用户模块】[DashSupport] 基于领域仓储查询用户增长走势, 周期: {}", period);
        int days = "30d".equalsIgnoreCase(period) ? 30 : 7;
        UserTrendStat trend = userRepository.countUserTrends(days);
        return UserTrendsVO.builder()
                .dates(trend != null && trend.getDates() != null ? trend.getDates() : Collections.emptyList())
                .newUserCountList(trend != null && trend.getNewUserCountList() != null ? trend.getNewUserCountList()
                        : Collections.emptyList())
                .build();
    }
}
