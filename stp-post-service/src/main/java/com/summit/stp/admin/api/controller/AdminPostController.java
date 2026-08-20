package com.summit.stp.admin.api.controller;

import com.summit.stp.admin.api.dto.AdminPostQueryRequest;
import com.summit.stp.admin.application.command.AdminPostQueryCommand;
import com.summit.stp.admin.application.service.AdminPostService;
import com.summit.stp.admin.application.vo.AdminPostVO;
import com.summit.stp.common.application.api.result.PageResult;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.post.application.vo.PostVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/a/post")
public class AdminPostController {
    private final AdminPostService adminPostService;

    @PostMapping("/list")
    public Result<PageResult<List<AdminPostVO>>> list(@RequestBody AdminPostQueryRequest request) {
        AdminPostQueryCommand command = AdminPostQueryCommand.builder()
                .postId(request.getPostId())
                .page(request.getPage())
                .pageSize(request.getPageSize())
                .keyword(request.getKeyword())
                .status(request.getStatus())
                .creatorId(request.getCreatorId())
                .likeCount(request.getLikeCount())
                .createTime(request.getCreateTime())
                .updateTime(request.getUpdateTime())
                .comment(request.getComment())
                .build();
        return adminPostService.list(command);
    }

    @PostMapping("/bypass")
    public Result<Void> bypass(Long id) {
        adminPostService.bypass(id);
        return Result.success();
    }

    @PostMapping("/bypass/not")
    public Result<Void> bypassNot(Long id, String reason) {
        adminPostService.bypassNot(id, reason);
        return Result.success();
    }

    @PostMapping("/ban")
    public Result<Void> ban(Long id) {
        adminPostService.toggleBan(id, true);
        return Result.success();
    }

    @PostMapping("/unban")
    public Result<Void> unban(Long id) {
        adminPostService.toggleBan(id, false);
        return Result.success();
    }

    @GetMapping("/detail")
    public Result<PostVO> detail(Long id, Integer status) {
        return Result.success(adminPostService.getPostById(id, status));
    }

}
