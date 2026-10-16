package com.summit.stp.message.admin.api.controller;

import com.summit.stp.message.admin.api.dto.AdminCreateNotificationRequest;
import com.summit.stp.message.admin.api.dto.AdminNotificationQueryRequest;
import com.summit.stp.message.admin.application.command.AdminCreateNotificationCommand;
import com.summit.stp.message.admin.application.command.AdminNotificationQueryCommand;
import com.summit.stp.message.admin.application.service.AdminNotificationService;
import com.summit.stp.message.admin.application.vo.AdminNotificationVO;
import com.summit.stp.common.annotation.Admin;
import com.summit.stp.common.annotation.Login;
import com.summit.stp.common.application.api.result.PageResult;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.operationlog.annotation.OperationLog;
import com.summit.stp.operationlog.domain.model.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Admin
@Login
@RestController
@RequestMapping("/a/notification")
@RequiredArgsConstructor
public class AdminSystemMessageController {
    private final AdminNotificationService adminNotificationService;

    @PostMapping("/list")
    public Result<PageResult<List<AdminNotificationVO>>> listBy(@RequestBody AdminNotificationQueryRequest request){
        AdminNotificationQueryCommand command = AdminNotificationQueryCommand.builder()
                .page(clampPage(request.getPage()))
                .size(clampSize(request.getSize()))
                .keyword(request.getKeyword())
                .noticeType(request.getNoticeType())
                .excludeDeleted(request.getExcludeDeleted())
                .build();
        return  adminNotificationService.listBy(command);
    }

    @PostMapping("/create")
    @OperationLog(description = "创建系统通知", type = Operation.OperationType.CREATE)
    public Result<Void> create(@RequestBody AdminCreateNotificationRequest request){
        AdminCreateNotificationCommand command = AdminCreateNotificationCommand.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .noticeType(request.getNoticeType())
                .targetType(request.getTargetType())
                .targetUserId(request.getTargetUserId())
                .imageUrls(request.getImageUrls())
                .build();
        return  adminNotificationService.create(command);
    }



    @PostMapping("/revoke/{id}")
    @OperationLog(description = "撤回系统通知", type = Operation.OperationType.UPDATE, entityId = "#id")
    public Result<Void> revoke(@PathVariable Long id){
        return  adminNotificationService.revoke(id);
    }
    @PostMapping("/public/{id}")
    @OperationLog(description = "发布系统通知", type = Operation.OperationType.UPDATE, entityId = "#id")
    public Result<Void> publish(@PathVariable Long id){
        return  adminNotificationService.publish(id);
    }

    @PostMapping("/delete/{id}")
    @OperationLog(description = "删除系统通知", type = Operation.OperationType.DELETE, entityId = "#id")
    public Result<Void> delete(@PathVariable Long id){
        return  adminNotificationService.delete(id);
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
