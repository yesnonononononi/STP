package com.summit.stp.admin.api.controller;

import com.summit.stp.common.annotation.Admin;
import com.summit.stp.common.annotation.Login;
import com.summit.stp.common.application.api.result.PageResult;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.operationlog.annotation.OperationLog;
import com.summit.stp.operationlog.domain.model.Operation;
import com.summit.stp.admin.application.command.CreateSystemActivityCommand;
import com.summit.stp.admin.application.command.SystemActivityQueryCommand;
import com.summit.stp.admin.application.service.SystemActivityAppService;
import com.summit.stp.admin.application.vo.SystemActivityVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Admin
@Login
@RestController
@RequestMapping("/a/system/activity")
@RequiredArgsConstructor
public class SystemActivityController {

    private final SystemActivityAppService systemActivityAppService;

    @PostMapping("/create")
    @OperationLog(description = "创建系统活动", type = Operation.OperationType.CREATE)
    public Result<Void> createActivity(@RequestBody CreateSystemActivityCommand command) {
        return systemActivityAppService.createActivity(command);
    }

    @GetMapping("/recent")
    public Result<List<SystemActivityVO>> getRecentActivities(@RequestParam(value = "limit", defaultValue = "10") int limit) {
        return systemActivityAppService.getRecentActivities((int) clampLimit(limit));
    }

    @PostMapping("/page")
    public Result<PageResult<List<SystemActivityVO>>> queryActivitiesPage(
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "module", required = false) String module,
            @RequestParam(value = "page", defaultValue = "1") long page,
            @RequestParam(value = "pageSize", defaultValue = "10") long pageSize) {
        SystemActivityQueryCommand command = SystemActivityQueryCommand.builder()
                .type(type)
                .module(module)
                .page(clampPage(page))
                .pageSize(clampSize(pageSize))
                .build();
        return systemActivityAppService.queryActivitiesPage(command);
    }

    private static long clampPage(long page) {
        return page < 1 ? 1 : page;
    }

    private static long clampSize(long size) {
        if (size < 1) {
            return 10;
        }
        return Math.min(size, 100);
    }

    private static long clampLimit(long limit) {
        if (limit < 1) {
            return 10;
        }
        return Math.min(limit, 100);
    }
}
