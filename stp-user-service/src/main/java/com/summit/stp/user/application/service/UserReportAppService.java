package com.summit.stp.user.application.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.admin.application.service.AdminUserService;
import com.summit.stp.common.application.api.result.PageResult;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.user.application.command.CreateUserReportCommand;
import com.summit.stp.user.application.command.UserReportQueryCommand;
import com.summit.stp.user.application.vo.UserReportVO;
import com.summit.stp.user.domain.model.User;
import com.summit.stp.user.domain.repository.UserRepository;
import com.summit.stp.user.domain.model.UserReport;
import com.summit.stp.user.domain.model.UserReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserReportAppService {

    private final UserReportRepository<UserReport> userReportRepository;
    private final UserRepository<User> userRepository;
    private final AdminUserService adminUserService;

    @Transactional(rollbackFor = Exception.class)
    public Result<Void> createReport(CreateUserReportCommand command, Long currentUserId) {
        log.info("【用户模块】[举报] 用户 {} 发起举报, 被举报用户ID: {}", currentUserId, command.getReportedId());
        if (currentUserId == null || command.getReportedId() == null) {
            return Result.error("举报参数非法");
        }
        if (currentUserId.equals(command.getReportedId())) {
            return Result.error("不能举报自己");
        }

        UserReport report = UserReport.builder()
                .reporterId(currentUserId)
                .reportedId(command.getReportedId())
                .reason(command.getReason())
                .evidence(command.getEvidence())
                .createTime(LocalDateTime.now())
                .build();

        userReportRepository.save(report);
        return Result.success();
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteReport(Long reportId) {
        log.info("【用户模块】[举报] 删除举报记录, ID: {}", reportId);
        userReportRepository.delete(reportId);
        return Result.success();
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Void> ignoreReport(Long reportId) {
        log.info("【用户模块】[举报] 忽略举报, ID: {}", reportId);
        Optional<UserReport> reportOpt = userReportRepository.findById(reportId);
        if (reportOpt.isEmpty()) {
            return Result.error("举报记录不存在");
        }
        UserReport report = reportOpt.get();
        report.ignore();
        userReportRepository.updateById(report);
        return Result.success();
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Void> processAndBanReport(Long reportId) {
        log.info("【用户模块】[举报] 处理举报并封禁用户, ID: {}", reportId);
        Optional<UserReport> reportOpt = userReportRepository.findById(reportId);
        if (reportOpt.isEmpty()) {
            return Result.error("举报记录不存在");
        }
        UserReport report = reportOpt.get();
        report.process();
        userReportRepository.updateById(report);

        // 调用 AdminUserService 执行用户封禁
        if (report.getReportedId() != null) {
            adminUserService.toggleBan(report.getReportedId(), true);
        }
        return Result.success();
    }

    public Result<PageResult<List<UserReportVO>>> queryReportPage(UserReportQueryCommand command) {
        Page<UserReport> reportPage = userReportRepository.queryReportsPage(
                command.getStatus(),
                command.getPage(),
                command.getPageSize()
        );

        List<UserReport> records = reportPage.getRecords();
        if (records == null || records.isEmpty()) {
            return Result.success(new PageResult<>(
                    reportPage.getCurrent(),
                    reportPage.getTotal(),
                    Collections.emptyList()
            ));
        }

        // 收集所有的举报人和被举报人ID，执行批量查询
        Set<Long> userIds = records.stream()
                .flatMap(r -> Stream.of(r.getReporterId(), r.getReportedId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, User> userMap = userRepository.findUserByIds(userIds);

        List<UserReportVO> voList = records.stream().map(r -> {
            User reporter = userMap.get(r.getReporterId());
            User reported = userMap.get(r.getReportedId());
            String reporterNick = reporter == null ? "未知用户" : reporter.getUsername().getValue();
            String reportedNick = reported == null ? "未知用户" : reported.getUsername().getValue();
            return UserReportVO.builder()
                    .id(r.getId())
                    .reporterId(r.getReporterId())
                    .reporterNick(reporterNick)
                    .reportedId(r.getReportedId())
                    .reportedNick(reportedNick)
                    .reason(r.getReason())
                    .evidence(r.getEvidence())
                    .status(r.getStatus() != null ? r.getStatus().getCode() : 0)
                    .statusDesc(r.getStatus() != null ? r.getStatus().getDescription() : "待处理")
                    .createTime(r.getCreateTime())
                    .build();
        }).toList();

        PageResult<List<UserReportVO>> pageResult = new PageResult<>(
                reportPage.getCurrent(),
                reportPage.getTotal(),
                voList
        );
        return Result.success(pageResult);
    }
}
