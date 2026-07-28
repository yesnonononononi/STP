package com.summit.stp.user.api;

import com.summit.stp.common.ThreadContext.UserHolder;
import com.summit.stp.common.annotation.Login;
import com.summit.stp.common.result.Result;
import com.summit.stp.user.api.dto.request.UpdateUserSettingRequest;
import com.summit.stp.user.application.command.UpdateUserSettingCommand;
import com.summit.stp.user.application.service.UserSettingAppService;
import com.summit.stp.common.application.vo.UserSettingVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/setting")
@RequiredArgsConstructor
@Api(tags = "用户配置管理")
public class UserSettingController {
    private final UserSettingAppService userSettingAppService;

    @Login
    @GetMapping("/current")
    @ApiOperation(value = "获取当前用户配置偏好", notes = "获取或初始化当前登录用户的配置偏好信息")
    public Result<UserSettingVO> getCurrentUserSetting() {
        Long currentUserId = UserHolder.getUser().getId();
        return Result.success(userSettingAppService.getUserSetting(currentUserId));
    }

    @Login
    @PostMapping("/update")
    @ApiOperation(value = "更新当前用户配置偏好", notes = "更新当前登录用户的显示已删除帖子及个性化推荐设置")
    public Result<Void> updateSetting(
            @ApiParam(value = "配置修改请求体", required = true) @RequestBody UpdateUserSettingRequest request) {
        Long currentUserId = UserHolder.getUser().getId();
        UpdateUserSettingCommand command = UpdateUserSettingCommand.builder()
                .userId(currentUserId)
                .showDelPost(request.getShowDelPost())
                .customizationRecommend(request.getCustomizationRecommend())
                .build();
        userSettingAppService.updateSetting(command);
        return Result.success();
    }
}
