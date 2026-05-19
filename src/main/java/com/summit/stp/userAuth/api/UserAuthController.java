package com.summit.stp.userAuth.api;

import com.summit.stp.shared.result.Result;
import com.summit.stp.shared.util.IpUtil;
import com.summit.stp.userAuth.api.dto.request.LoginRequest;
import com.summit.stp.userAuth.api.dto.request.RefreshTokenRequest;
import com.summit.stp.userAuth.api.dto.request.RegisterRequest;
import com.summit.stp.userAuth.application.UserAuthApplicationService;
import com.summit.stp.userAuth.application.command.LoginCommand;
import com.summit.stp.userAuth.application.command.RefreshTokenCommand;
import com.summit.stp.userAuth.application.command.RegisterCommand;
import com.summit.stp.userAuth.application.vo.LoginVO;
import com.summit.stp.userAuth.application.vo.RefreshTokenVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user-auth")
@RequiredArgsConstructor
public class UserAuthController {
    private final UserAuthApplicationService userAuthApplicationService;

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginRequest request, HttpServletRequest httpServletRequest) {
        LoginCommand command = LoginCommand.builder()
            .username(request.getUsername())
            .password(request.getPassword())
            .ip(IpUtil.getIpAddr(httpServletRequest))
            .build();
            
        return userAuthApplicationService.login(command);
    }

    @PostMapping("/refresh-token")
    public Result<RefreshTokenVO> refreshToken(@RequestBody RefreshTokenRequest request, HttpServletRequest httpServletRequest) {
        RefreshTokenCommand command = RefreshTokenCommand.builder()
            .refreshToken(request.getRefreshToken())
            .username(request.getUsername())
            .ip(IpUtil.getIpAddr(httpServletRequest))
            .build();
            
        return userAuthApplicationService.refreshToken(command);
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        userAuthApplicationService.logout();
        return Result.success();
    }

    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterRequest request){
        RegisterCommand command = RegisterCommand.builder()
            .phoneNumber(request.getPhoneNumber())
            .password(request.getPassword())
            .verifyCode(request.getVerifyCode())
            .build();
        userAuthApplicationService.register(command);
        return Result.success();
    }
}
