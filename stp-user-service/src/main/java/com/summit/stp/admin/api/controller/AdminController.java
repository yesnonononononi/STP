package com.summit.stp.admin.api.controller;

import com.summit.stp.admin.api.dto.AdminDTO;
import com.summit.stp.admin.application.command.AddAdminCommand;
import com.summit.stp.admin.application.service.AdminService;
import com.summit.stp.admin.application.vo.AdminVO;
import com.summit.stp.operationlog.annotation.OperationLog;
import com.summit.stp.operationlog.domain.model.Operation;
import com.summit.stp.common.annotation.Admin;
import com.summit.stp.common.annotation.Login;
import com.summit.stp.common.application.api.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/u/admin")

public class AdminController {
    private final AdminService adminService;
    @Admin
    @GetMapping("/list")
    @Login
    public Result<List<AdminVO>> list(Integer page, Integer pageSize){
        return adminService.list(page, pageSize);
    }
    @Admin
    @PostMapping("/ban")
    @Login
    @OperationLog(description = "禁用管理员", type = Operation.OperationType.UPDATE)
    public Result<Void> ban(Long aId){
        return adminService.toggleBan(aId, true);
    }
    @Admin
    @PostMapping("/unban")
    @Login
    @OperationLog(description = "启用管理员", type = Operation.OperationType.UPDATE ,entityId ="#aId")
    public Result<Void> unban(Long aId){
        return adminService.toggleBan(aId, false);
    }
    @Admin
    @PostMapping("/ascend")
    @Login
    @OperationLog(description = "提升管理员等级", type = Operation.OperationType.UPDATE,entityId = "#aId")
    public Result<Void> ascend(Long aId){
        return adminService.changeOrder(aId, true);
    }
    @Admin
    @PostMapping("/descend")
    @Login
    @OperationLog(description = "降低管理员等级", type = Operation.OperationType.UPDATE,entityId = "#aId")
    public Result<Void> descend(Long aId){
        return adminService.changeOrder(aId, false);
    }

    @GetMapping("/internal/is/{uid}")
    public Integer isAdmin(@PathVariable Long uid){
        return adminService.is(uid);
    }
    @Admin
    @PostMapping("/add")
    @Login
    @OperationLog(description = "新增管理员", type = Operation.OperationType.CREATE,entityId = "#adminDTO.getUid()")
    public Result<Void> add(@RequestBody AdminDTO adminDTO){
        AddAdminCommand command = AddAdminCommand.builder()
                .uid(adminDTO.getUid())
                .order(adminDTO.getOrder())
                .build();
        return adminService.add(command);
    }

}
