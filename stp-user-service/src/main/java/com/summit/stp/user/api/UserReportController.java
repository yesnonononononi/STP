package com.summit.stp.user.api;

import com.summit.stp.common.annotation.Login;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.common.auth.UserHolder;
import com.summit.stp.operationlog.annotation.OperationLog;
import com.summit.stp.operationlog.domain.model.Operation;
import com.summit.stp.user.api.dto.CreateUserReportDTO;
import com.summit.stp.user.application.command.CreateUserReportCommand;
import com.summit.stp.user.application.service.UserReportAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/report")
@RequiredArgsConstructor
public class UserReportController {

    private final UserReportAppService userReportAppService;

    @Login
    @PostMapping
    @OperationLog(description = "新建用户举报", type = Operation.OperationType.CREATE)
    public Result<Void> createReport(@RequestBody CreateUserReportDTO dto) {
        Long currentUserId = UserHolder.getUser().getId();
        CreateUserReportCommand command = CreateUserReportCommand.builder()
                .reportedId(dto.getReportedId())
                .evidence(dto.getEvidence())
                .reason(dto.getReason())
                .build();
        return userReportAppService.createReport(command, currentUserId);
    }

    @Login
    @PostMapping("/delete/{id}")
    @OperationLog(description = "删除用户举报", type = Operation.OperationType.DELETE, entityId = "#id")
    public Result<Void> deleteReport(@PathVariable("id") Long id) {
        return userReportAppService.deleteReport(id);
    }

    @Login
    @PostMapping("/ignore/{id}")
    @OperationLog(description = "忽略用户举报", type = Operation.OperationType.UPDATE, entityId = "#id")
    public Result<Void> ignoreReport(@PathVariable("id") Long id) {
        return userReportAppService.ignoreReport(id);
    }
}
