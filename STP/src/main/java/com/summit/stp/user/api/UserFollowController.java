package com.summit.stp.user.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.summit.stp.shared.result.Result;
import com.summit.stp.user.api.dto.request.CreateUserFollowRequest;
import com.summit.stp.user.api.dto.request.UpdateUserFollowRequest;
import com.summit.stp.user.application.command.CreateUserFollowCommand;
import com.summit.stp.user.application.command.UpdateUserFollowCommand;
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

    @GetMapping("/{id}")
    @ApiOperation(value = "获取关注详情", notes = "根据主键ID获取关注关系的具体数据")
    public Result<UserFollowVO> getUserFollow(
            @ApiParam(value = "主键ID", required = true) @PathVariable Long id) {
        return Result.success(userFollowAppService.getUserFollowById(id));
    }

    @PostMapping("/follow")
    @ApiOperation(value = "关注/取消关注用户 (Toggle开关)", notes = "一键切换关注关系：若未关注则执行关注，若已关注则执行取消关注，实现状态互转并异步更新粉丝数。")
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

    @PutMapping("/update")
    @ApiOperation(value = "更新关注关系", notes = "修改关注的状态或来源")
    public Result<Void> updateFollow(
            @ApiParam(value = "更新关注参数", required = true) @RequestBody UpdateUserFollowRequest request) {
        UpdateUserFollowCommand command = UpdateUserFollowCommand.builder()
                .id(request.getId())
                .status(request.getStatus())
                .source(request.getSource())
                .build();
        userFollowAppService.updateFollow(command);
        return Result.success();
    }

    @DeleteMapping("/unfollow/{id}")
    @ApiOperation(value = "取消关注", notes = "根据主键ID取消关注")
    public Result<Void> unfollow(
            @ApiParam(value = "主键ID", required = true) @PathVariable Long id) {
        userFollowAppService.unfollow(id);
        return Result.success();
    }

    @GetMapping("/followers")
    @ApiOperation(value = "分页获取粉丝列表", notes = "根据用户ID分页获取该用户的粉丝列表")
    public Result<Page<UserFollowVO>> getFollowers(
            @ApiParam(value = "用户ID", required = true) @RequestParam Long userId,
            @ApiParam(value = "页码 (默认1)", required = false) @RequestParam(defaultValue = "1") long page,
            @ApiParam(value = "每页大小 (默认10)", required = false) @RequestParam(defaultValue = "10") long pageSize) {
        return Result.success(userFollowAppService.getFollowersPage(userId, page, pageSize));
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
