package com.summit.stp.comment.admin.api.controller;

import com.summit.stp.comment.admin.api.request.CommentQueryRequest;
import com.summit.stp.comment.admin.application.command.CommentQueryCommand;
import com.summit.stp.comment.admin.application.service.AdminCommentService;
import com.summit.stp.comment.admin.application.vo.AdminCommentVO;
import com.summit.stp.common.annotation.Admin;
import com.summit.stp.common.annotation.Login;
import com.summit.stp.common.application.api.result.PageResult;
import com.summit.stp.common.application.api.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Admin
@Login
@RequiredArgsConstructor
@RestController
@RequestMapping("/a/comment")
public class AdminCommentController {
    private final AdminCommentService adminCommentService;
    @PostMapping("/list")
    public Result<PageResult<List<AdminCommentVO>>> listBy(@RequestBody CommentQueryRequest request){
        CommentQueryCommand command = CommentQueryCommand
                .builder()
                .page(request.getPage())
                .pageSize(request.getPageSize())
                .keyword(request.getKeyword())
                .status(request.getStatus())
                .build();
        return adminCommentService.listBy(command);
    }
    @PostMapping("/ban")
    public Result<Void> ban(Long id){
        return adminCommentService.toggleBan(id,true);
    }
    @PostMapping("/unban")
    public Result<Void> unban(Long id){
        return adminCommentService.toggleBan(id,false);
    }
    @PostMapping("/report/ignore/{id}")
    public Result<Void> ignore(@PathVariable Long id){
        return adminCommentService.ignoreReport(id);
    }
}
