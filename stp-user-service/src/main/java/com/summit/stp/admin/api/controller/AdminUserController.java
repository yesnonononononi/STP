package com.summit.stp.admin.api.controller;

import com.summit.stp.admin.api.dto.AdminUserQueryDTO;
import com.summit.stp.admin.application.command.AdminUserQueryCommand;
import com.summit.stp.admin.application.service.AdminUserService;
import com.summit.stp.common.application.api.result.PageResult;
import com.summit.stp.operationlog.annotation.OperationLog;
import com.summit.stp.operationlog.domain.model.Operation;
import com.summit.stp.common.annotation.Admin;
import com.summit.stp.common.annotation.Login;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.user.api.vo.UserProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@Admin
@RequiredArgsConstructor
@Login
@RequestMapping("/a/user")
public class AdminUserController {
    private final AdminUserService adminUserService;

    @PostMapping("/list")
    public Result<PageResult<List<UserProfileVO>>> listBy(@RequestBody AdminUserQueryDTO dto){
        AdminUserQueryCommand command = dto.toCommand();
        command.setPage(clampPage(command.getPage()));
        command.setSize(clampSize(command.getSize()));
        return adminUserService.listBy(command);
    }

    @GetMapping("/count/new")
    public Result<Long> countNewUser(){
        return adminUserService.countNewUser();
    }

    @PostMapping("/ban")
    @OperationLog(description = "禁用用户", type =  Operation.OperationType.UPDATE,entityId = "#uid")
    public Result<Void> ban(Long uid){
        return adminUserService.toggleBan(uid,true);
    }


    @OperationLog(description = "启用用户", type =  Operation.OperationType.UPDATE,entityId = "#uid")
    @PostMapping("/unban")
    public Result<Void> unban(Long uid){
        return adminUserService.toggleBan(uid,false);
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
