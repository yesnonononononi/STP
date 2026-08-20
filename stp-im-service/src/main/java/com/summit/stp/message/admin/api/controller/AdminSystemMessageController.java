package com.summit.stp.message.admin.api.controller;

import com.summit.stp.message.admin.api.dto.AdminCreateNotificationRequest;
import com.summit.stp.message.admin.api.dto.AdminNotificationQueryRequest;
import com.summit.stp.message.admin.application.command.AdminCreateNotificationCommand;
import com.summit.stp.message.admin.application.command.AdminNotificationQueryCommand;
import com.summit.stp.message.admin.application.service.AdminNotificationService;
import com.summit.stp.message.admin.application.vo.AdminNotificationVO;
import com.summit.stp.common.application.api.result.PageResult;
import com.summit.stp.common.application.api.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/a/notification")
@RequiredArgsConstructor
public class AdminSystemMessageController {
    private final AdminNotificationService adminNotificationService;

    @PostMapping("/list")
    public Result<PageResult<List<AdminNotificationVO>>> listBy(@RequestBody AdminNotificationQueryRequest request){
        AdminNotificationQueryCommand command = AdminNotificationQueryCommand.builder()
                .page(request.getPage())
                .size(request.getSize())
                .keyword(request.getKeyword())
                .noticeType(request.getNoticeType())
                .excludeDeleted(request.getExcludeDeleted())
                .build();
        return  adminNotificationService.listBy(command);
    }

    @PostMapping("/create")
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
    public Result<Void> revoke(@PathVariable Long id){
        return  adminNotificationService.revoke(id);
    }
    @PostMapping("/public/{id}")
    public Result<Void> publish(@PathVariable Long id){
        return  adminNotificationService.publish(id);
    }

    @PostMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id){
        return  adminNotificationService.delete(id);
    }
}
