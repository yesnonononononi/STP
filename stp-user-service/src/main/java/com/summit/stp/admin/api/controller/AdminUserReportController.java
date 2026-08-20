package com.summit.stp.admin.api.controller;

import com.summit.stp.common.annotation.Admin;
import com.summit.stp.common.annotation.Login;
import com.summit.stp.common.application.api.result.PageResult;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.operationlog.annotation.OperationLog;
import com.summit.stp.operationlog.domain.model.Operation;
import com.summit.stp.user.application.command.UserReportQueryCommand;
import com.summit.stp.user.application.service.UserReportAppService;
import com.summit.stp.user.application.vo.UserReportVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Admin
@Login
@RestController
@RequestMapping("/a/user/report")
@RequiredArgsConstructor
public class AdminUserReportController {

    private final UserReportAppService userReportAppService;

    @PostMapping("/list")
    public Result<PageResult<List<UserReportVO>>> queryReportPage(
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "page", defaultValue = "1") long page,
            @RequestParam(value = "pageSize", defaultValue = "10") long pageSize) {
        UserReportQueryCommand command = UserReportQueryCommand.builder()
                .status(status)
                .page(page)
                .pageSize(pageSize)
                .build();
        return userReportAppService.queryReportPage(command);
    }

    @PostMapping("/ignore/{id}")
    @OperationLog(description = "管理员忽略用户举报", type = Operation.OperationType.UPDATE, entityId = "#id")
    public Result<Void> ignoreReport(@PathVariable("id") Long id) {
        return userReportAppService.ignoreReport(id);
    }

    @PostMapping("/process/{id}")
    @OperationLog(description = "管理员处理举报并封禁用户", type = Operation.OperationType.UPDATE, entityId = "#id")
    public Result<Void> processAndBanReport(@PathVariable("id") Long id) {
        return userReportAppService.processAndBanReport(id);
    }
}
