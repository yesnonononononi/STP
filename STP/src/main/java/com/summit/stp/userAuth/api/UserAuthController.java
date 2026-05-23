package com.summit.stp.userAuth.api;

import com.summit.stp.shared.result.Result;
import com.summit.stp.shared.util.IpUtil;
import com.summit.stp.userAuth.api.dto.request.ForgetRequest;
import com.summit.stp.userAuth.api.dto.request.LoginRequest;
import com.summit.stp.userAuth.api.dto.request.RefreshTokenRequest;
import com.summit.stp.userAuth.api.dto.request.RegisterRequest;
import com.summit.stp.userAuth.application.UserAuthApplicationService;
import com.summit.stp.userAuth.application.command.ForgetCommand;
import com.summit.stp.userAuth.application.command.LoginCommand;
import com.summit.stp.userAuth.application.command.RefreshTokenCommand;
import com.summit.stp.userAuth.application.command.RegisterCommand;
import com.summit.stp.userAuth.application.vo.LoginVO;
import com.summit.stp.userAuth.application.vo.RefreshTokenVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user-auth")
@RequiredArgsConstructor
@Api(tags = "用户认证管理")
public class UserAuthController {
    private final UserAuthApplicationService userAuthApplicationService;

    @PostMapping("/login")
    @ApiOperation(value = "用户登录", notes = "根据用户名/手机号和密码获取访问Token和刷新Token")
    public Result<LoginVO> login(
            @ApiParam(value = "用户登录请求参数", required = true) @RequestBody LoginRequest request,
            HttpServletRequest httpServletRequest) {
        LoginCommand command = LoginCommand.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .ip(IpUtil.getIpAddr(httpServletRequest))
                .build();

        return userAuthApplicationService.login(command);
    }

    @PostMapping("/refresh-token")
    @ApiOperation(value = "刷新Token", notes = "当访问Token失效时，使用刷新Token获取新的访问Token")
    public Result<RefreshTokenVO> refreshToken(
            @ApiParam(value = "刷新Token请求参数", required = true) @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpServletRequest) {
        RefreshTokenCommand command = RefreshTokenCommand.builder()
                .refreshToken(request.getRefreshToken())
                .username(request.getUsername())
                .ip(IpUtil.getIpAddr(httpServletRequest))
                .build();

        return userAuthApplicationService.refreshToken(command);
    }


    @PostMapping("/forget")
    @ApiOperation(value = "忘记/重置密码", notes = "通过短信验证码找回并修改密码")
    public Result<Void> forget(
            @ApiParam(value = "重置密码请求参数", required = true) @RequestBody ForgetRequest request) {
        ForgetCommand command = ForgetCommand.builder()
                .phone(request.getPhoneNumber())
                .password(request.getPassword())
                .verifyCode(request.getVerifyCode())
                .resetType(request.getResetType())
                .build();
        userAuthApplicationService.forget(command);
        return Result.success();
    }

    @PostMapping("/logout")
    @ApiOperation(value = "用户登出", notes = "注销当前登录状态，废弃Token")
    public Result<Void> logout() {
        userAuthApplicationService.logout();
        return Result.success();
    }

    @PostMapping("/register")
    @ApiOperation(value = "用户注册", notes = "使用手机号和验证码注册新账户")
    public Result<Void> register(
            @ApiParam(value = "用户注册请求参数", required = true) @RequestBody RegisterRequest request) {
        RegisterCommand command = RegisterCommand.builder()
                .phoneNumber(request.getPhoneNumber())
                .password(request.getPassword())
                .verifyCode(request.getVerifyCode())
                .build();
        userAuthApplicationService.register(command);
        return Result.success();
    }
}
