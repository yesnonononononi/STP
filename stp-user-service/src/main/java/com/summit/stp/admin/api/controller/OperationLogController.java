package com.summit.stp.admin.api.controller;

import com.summit.stp.operationlog.domain.service.OperationLogService;
import com.summit.stp.operationlog.api.vo.OperationLogVO;
import com.summit.stp.common.application.api.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/a/logs")
public class OperationLogController {
    private final OperationLogService operationLogService;

    @GetMapping("/list")
    public Result<List<OperationLogVO>> listBy(String type, Long uid, Integer page, Integer size){
        return operationLogService.listBy(type, uid, page, size);
    }

    @PostMapping("/delete")
    public Result<Void> delete(@RequestBody List<Long> ids){
        return operationLogService.delete(ids);
    }
}
