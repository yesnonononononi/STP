package com.summit.stp.user.api;

import com.summit.stp.common.ThreadContext.UserHolder;
import com.summit.stp.common.annotation.Login;
import com.summit.stp.common.application.api.vo.UserProfileVO;
import com.summit.stp.common.application.api.vo.UserSimpleVO;
import com.summit.stp.common.result.Result;
import com.summit.stp.user.api.dto.request.UserPasswordUpdateRequest;
import com.summit.stp.user.api.dto.request.UserPhoneBindRequest;
import com.summit.stp.user.api.dto.request.UserProfileUpdateRequest;
import com.summit.stp.user.application.UserApplicationService;
import com.summit.stp.user.application.command.UserPasswordUpdateCommand;
import com.summit.stp.user.application.command.UserPhoneBindCommand;
import com.summit.stp.user.application.command.UserProfileUpdateCommand;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Api(tags = "用户基本信息管理")
public class UserController {
    private final UserApplicationService userApplicationService;

    @Login
    @PostMapping("/profile/update")
    @ApiOperation(value = "修改用户基本资料", notes = "修改用户的头像、昵称、个人简介和绑定邮箱")
    public Result<Void> updateProfile(
            @ApiParam(value = "基本资料修改请求体", required = true) @RequestBody UserProfileUpdateRequest request) {
        UserProfileUpdateCommand command = UserProfileUpdateCommand.builder()
                .nick(request.getNick())
                .avatar(request.getAvatar())
                .email(request.getEmail())
                .verifyCode(request.getVerifyCode())
                .introduce(request.getIntroduction())
                .gender(request.getGender())
                .age(request.getAge())
                .bgImage(request.getBgImage())
                .build();
        userApplicationService.updateProfile(command);
        return Result.success();
    }

    @Login
    @PostMapping("/phone/bind")
    @ApiOperation(value = "绑定或换绑手机号", notes = "通过新手机号以及对应的短信验证码进行绑定/重置")
    public Result<Void> bindPhone(
            @ApiParam(value = "手机绑定请求体", required = true) @RequestBody UserPhoneBindRequest request) {
        UserPhoneBindCommand command = UserPhoneBindCommand.builder()
                .phoneNumber(request.getPhoneNumber())
                .verifyCode(request.getVerifyCode())
                .build();
        userApplicationService.bindPhone(command);
        return Result.success();
    }

    @Login
    @PostMapping("/password/update")
    @ApiOperation(value = "修改登录密码", notes = "根据旧密码和新密码重置登录密码")
    public Result<Void> updatePassword(
            @ApiParam(value = "密码修改请求体", required = true) @RequestBody UserPasswordUpdateRequest request) {
        UserPasswordUpdateCommand command = UserPasswordUpdateCommand.builder()
                .oldPassword(request.getOldPassword())
                .newPassword(request.getNewPassword())
                .build();
        userApplicationService.updatePassword(command);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "获取用户信息", notes = "根据用户ID获取个人公开的属性资料")
    public Result<UserProfileVO> findUserById(
            @ApiParam(value = "用户ID", required = true) @PathVariable Long id) {
        return Result.success(userApplicationService.findUserById(id));
    }

    @Login
    @GetMapping("/current")
    @ApiOperation(value = "获取当前登录用户信息", notes = "根据当前登录会话上下文获取本人的详细资料")
    public Result<UserProfileVO> findCurrentUser() {
        Long currentUserId = UserHolder.getUser().getId();
        return Result.success(userApplicationService.findUserById(currentUserId));
    }

    @GetMapping("/simple/{id}")
    @ApiOperation(value = "获取用户简单公开展示信息", notes = "根据用户ID获取基础公开信息（如昵称、头像、粉丝、话题数、获赞数）")
    public Result<UserSimpleVO> findSimpleUserById(
            @ApiParam(value = "用户ID", required = true) @PathVariable Long id) {
        return Result.success(userApplicationService.findSimpleUserById(id));
    }
}
