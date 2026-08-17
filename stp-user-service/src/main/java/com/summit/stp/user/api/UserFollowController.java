package com.summit.stp.user.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.common.annotation.Login;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.user.api.dto.request.CreateUserFollowRequest;
import com.summit.stp.user.application.command.CreateUserFollowCommand;
import com.summit.stp.user.application.service.UserFollowAppService;
import com.summit.stp.user.application.vo.UserFollowVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/follow")
@RequiredArgsConstructor
@Api(tags = "用户关注关系管理")
public class UserFollowController {
    private final UserFollowAppService userFollowAppService;

    @Login
    @PostMapping("/follow")
    @ApiOperation(value = "关注/取消关注用户 (Toggle开关)", notes = "一键切换关注关系：若未关注则执行关注，若已关注则执行取消关注，实现状态互转并更新统计。")
    public Result<Void> follow(
            @ApiParam(value = "关注/取消关注请求参数", required = true) @RequestBody CreateUserFollowRequest request) {
        CreateUserFollowCommand command = CreateUserFollowCommand.builder()
                .followerId(request.getFollowerId())
                .followeeId(request.getFolloweeId())
                .source(request.getSource())
                .build();
        userFollowAppService.follow(command);
        return Result.success();
    }

    @Login
    @DeleteMapping("/unfollow/{id}")
    @ApiOperation(value = "取消关注", notes = "根据主键ID取消关注")
    public Result<Void> unfollow(
            @ApiParam(value = "主键ID", required = true) @PathVariable Long id) {
        userFollowAppService.unfollow(id);
        return Result.success();
    }

    @GetMapping("/followees")
    @ApiOperation(value = "分页获取关注列表", notes = "根据用户ID分页获取该用户关注的人的列表")
    public Result<Page<UserFollowVO>> getFollowees(
            @ApiParam(value = "用户ID", required = true) @RequestParam Long userId,
            @ApiParam(value = "页码 (默认1)", required = false) @RequestParam(defaultValue = "1") long page,
            @ApiParam(value = "每页大小 (默认10)", required = false) @RequestParam(defaultValue = "10") long pageSize) {
        return Result.success(userFollowAppService.getFolloweesPage(userId, page, pageSize));
    }
}
