package com.summit.stp.userAuth.application.service;

import com.summit.stp.shared.result.Result;
import com.summit.stp.userAuth.application.UserAuthApplicationService;
import com.summit.stp.userAuth.application.command.LoginCommand;
import com.summit.stp.userAuth.application.command.RefreshTokenCommand;
import com.summit.stp.userAuth.application.command.RegisterCommand;
import com.summit.stp.userAuth.application.vo.LoginVO;
import com.summit.stp.userAuth.application.vo.RefreshTokenVO;
import com.summit.stp.userAuth.domain.model.AuthToken;
import com.summit.stp.userAuth.domain.model.AuthUser;
import com.summit.stp.userAuth.domain.service.UserAuthDomainService;
import com.summit.stp.userAuth.infrastructure.config.AuthProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAuthAppServiceImpl implements UserAuthApplicationService {
    private final UserAuthDomainService userAuthDomainService;
    private final AuthProperties authProperties;

    @Override
    public Result<LoginVO> login(LoginCommand command) {
        AuthUser user = userAuthDomainService.authenticate(
                command.getUsername(),
                command.getPassword()
        );

        AuthToken authToken = userAuthDomainService.issueToken(
                user.getUsernameValue(),
                command.getIp(),
                authProperties.getTokenExpireSeconds(),
                true
        );

        LoginVO loginVO = LoginVO.builder()
                .token(authToken.getAccessToken())
                .refreshToken(authToken.getRefreshToken())
                .username(user.getUsernameValue())
                .expireTime(authProperties.getTokenExpireSeconds())
                .build();

        return Result.success(loginVO);
    }

    @Override
    public Result<RefreshTokenVO> refreshToken(RefreshTokenCommand refreshTokenCommand) {
        log.info("用户刷新 Token 请求: {}", refreshTokenCommand.getUsername());

        // 校验 Refresh Token 是否有效
        userAuthDomainService.isValid(
                refreshTokenCommand.getRefreshToken(),
                refreshTokenCommand.getUsername()
        );

        AuthToken authToken = userAuthDomainService.issueToken(
                refreshTokenCommand.getUsername(),
                refreshTokenCommand.getIp(),
                authProperties.getTokenExpireSeconds(),
                false
        );

        RefreshTokenVO refreshTokenVO = RefreshTokenVO.builder()
                .token(authToken.getAccessToken())
                .refreshToken(null) // 返回 null，代表前端不需要更新本地存储的 Refresh Token
                .build();

        return Result.success(refreshTokenVO);
    }

    @Override
    public void logout() {
        userAuthDomainService.logout();
    }

    @Override
    @Transactional
    public void register(RegisterCommand command) {
        log.info("用户注册请求: {}", command.getPhoneNumber());
        userAuthDomainService.register(
                command.getPhoneNumber(),
                command.getPassword(),
                command.getVerifyCode()
        );
        log.info("用户注册成功: {}", command.getPhoneNumber());
    }
}
