package com.summit.stp.admin.api.controller;

import com.summit.stp.operationlog.domain.service.OperationLogService;
import com.summit.stp.operationlog.api.vo.OperationLogVO;
import com.summit.stp.operationlog.annotation.OperationLog;
import com.summit.stp.operationlog.domain.model.Operation;
import com.summit.stp.common.annotation.Admin;
import com.summit.stp.common.annotation.Login;
import com.summit.stp.common.application.api.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Admin
@Login
@RequiredArgsConstructor
@RestController
@RequestMapping("/a/logs")
public class OperationLogController {
    private final OperationLogService operationLogService;

    @GetMapping("/list")
    public Result<List<OperationLogVO>> listBy(String type, Long uid, Integer page, Integer size){
        return operationLogService.listBy(type, uid, clampPage(page), clampSize(size));
    }

    @PostMapping("/delete")
    @OperationLog(description = "删除操作日志", type = Operation.OperationType.DELETE)
    public Result<Void> delete(@RequestBody List<Long> ids){
        return operationLogService.delete(ids);
    }

    private static Integer clampPage(Integer page) {
        return (page == null || page < 1) ? 1 : page;
    }

    private static Integer clampSize(Integer size) {
        if (size == null || size < 1) {
            return 10;
        }
        return Math.min(size, 100);
    }
}
