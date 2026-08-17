package com.summit.stp.userAuth.application.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.summit.stp.common.application.domain.exception.BusinessException;
import com.summit.stp.common.auth.UserHolder;
import com.summit.stp.common.application.domain.model.Password;
import com.summit.stp.common.application.domain.model.UserSession;
import com.summit.stp.common.constants.UserAuthConstants;
import com.summit.stp.user.api.client.UserFeignClient;
import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.common.util.IpUtil;
import com.summit.stp.userAuth.application.command.ForgetCommand;
import com.summit.stp.userAuth.application.command.LoginCommand;
import com.summit.stp.userAuth.application.command.RefreshTokenCommand;
import com.summit.stp.userAuth.application.command.RegisterCommand;
import com.summit.stp.userAuth.application.vo.LoginVO;
import com.summit.stp.userAuth.application.vo.RefreshTokenVO;
import com.summit.stp.userAuth.domain.exception.ResetPasswordException;
import com.summit.stp.userAuth.domain.exception.UserNotFoundException;
import com.summit.stp.userAuth.domain.model.AuthUser;
import com.summit.stp.userAuth.domain.model.ResetType;
import com.summit.stp.userAuth.domain.repository.AuthUserRepository;
import com.summit.stp.userAuth.domain.repository.TokenRepository;
import com.summit.stp.userAuth.domain.service.ResetPasswordStrategy;
import com.summit.stp.userAuth.domain.service.ResetStrategyRegistry;
import com.summit.stp.userAuth.domain.service.UserAuthDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAuthAppServiceImpl implements UserAuthApplicationService {
    private final UserAuthDomainService userAuthDomainService;
    private final ResetStrategyRegistry resetStrategyRegistry;
    private final AuthUserRepository authUserRepository;
    private final TokenRepository tokenRepository;
    private final PlatformTransactionManager transactionManager;

    private final UserFeignClient userFeignClient;
    private final com.summit.stp.user.api.client.AdminFeignClient adminFeignClient;


    @Override
    public Result<LoginVO> login(LoginCommand command) {
        // 核心密码与状态激活的身份核对
        AuthUser user = userAuthDomainService.authenticate(
                command.getUsername(),
                command.getPassword()
        );

        Integer adminOrder = null;
        Long userId = user.getUserId();
        try {
            adminOrder = adminFeignClient.isAdmin(userId);
        } catch (Exception e) {
            log.error("【登录】远程调用判断管理员身份失败, userId: " + userId, e);
        }

        UserSession userSession = UserSession.builder()
                .id(userId)
                .username(user.getUsername().getValue())
                .ip(command.getIp())
                .loginTime(LocalDateTime.now())
                .admin(adminOrder)
                .tokenType(UserSession.TokenType.ACCESS)
                .build();

        //签发双 Token、管理 Redis 映射、维护 Session
        Map.Entry<String, String> entry = tokenRepository.generateToken(userSession);

        // 通过 Feign 调用 user-service 异步更新用户 IP 地理归属地 information
        try {
            String ipLocation = IpUtil.toString(command.getIp());
            authUserRepository.updateUserIp(userId, ipLocation);
        } catch (Exception e) {
            log.error("【登录】更新用户 IP 地理归属地失败, userId: " + userId, e);
        }

        LoginVO loginVO = LoginVO.builder()
                .token(entry.getKey())
                .refreshToken(entry.getValue())
                .username(user.getUsername().getValue())
                .expireTime(UserAuthConstants.Business.DEFAULT_TOKEN_EXPIRE_SECONDS)
                .build();

        return Result.success(loginVO);
    }

    @Override
    public Result<RefreshTokenVO> refreshToken(RefreshTokenCommand refreshTokenCommand) {

        if (StrUtil.equals(refreshTokenCommand.getRefreshToken(), UserAuthConstants.Business.JWT_FALLBACK, true)) {
            return Result.error("令牌无效,请尝试重新登录");
        }
        String username = refreshTokenCommand.getUsername();
        String newAccessToken = IdUtil.fastSimpleUUID();
        String oldAccessToken = refreshTokenCommand.getOldAccessToken();
        String refreshToken = refreshTokenCommand.getRefreshToken();
        log.info("【Token-refresh】用户刷新 Token 请求: {}", username);

        Long userId = refreshTokenCommand.getUserId();
        AuthUser user    = authUserRepository.findUserByOrThrow(username, userId, null, UserNotFoundException::new);

        Long uidFromDb = user.getUserId();
        Integer  adminOrder = adminFeignClient.isAdmin(uidFromDb);


        UserSession userSession = UserSession.builder()
                .username(username)
                .token(newAccessToken)
                .ip(refreshTokenCommand.getIp())
                .id(uidFromDb)
                .loginTime(LocalDateTime.now())
                .admin(adminOrder)
                .tokenType(UserSession.TokenType.ACCESS)
                .build();


        if (!tokenRepository.refreshToken(oldAccessToken, refreshToken, newAccessToken, userSession)) {
            return Result.error("刷新令牌已过期,请重新登录");
        }

        RefreshTokenVO refreshTokenVO = RefreshTokenVO.builder()
                .token(newAccessToken)
                .refreshToken(refreshToken)
                .build();
        log.info("【Token-refresh】 success :{}", username);

        return Result.success(refreshTokenVO);
    }

    @Override
    public void logout(String oldAccessToken, String oldRefreshToken) {
        // 登出操作的流程编排 (清理 Token 以及 Session 缓存)
        UserSession user = UserHolder.getUser();
        tokenRepository.logout(oldAccessToken, oldRefreshToken, user.getUsername());
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Override
    public Result<Void> register(RegisterCommand command) {
        log.info("用户注册请求: {}", command.getPhoneNumber());

        // 1. 校验验证码
        resetStrategyRegistry.getResetStrategy(ResetType.PHONE).verify(command.getPhoneNumber(), command.getVerifyCode());
        try {
            // 2. 注册
            Result<Void> registerResult = userFeignClient.register(command.getPhoneNumber(), command.getPassword());
            if (registerResult != null && !registerResult.isSuccess()) {
                return Result.error(registerResult.getErrMsg());
            }
        } catch (DuplicateKeyException e) {
            return Result.error("用户已注册");
        }

        log.info("用户注册成功: {}", command.getPhoneNumber());
        return Result.success();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void forget(ForgetCommand command) {
        // 1. 策略安全验证与降级
        Integer resetCode = command.getResetType();
        String password = command.getPassword();
        ResetType resetType;
        if(StrUtil.isBlank(password))throw new BusinessException("密码不能为空");
        if (resetCode == null || (resetType = ResetType.fromCode(resetCode)) == null) {
            throw new ResetPasswordException("无效的找回方式");
        }

        // 2. 获取并分发对应策略进行凭证校验
        ResetPasswordStrategy resetStrategy = resetStrategyRegistry.getResetStrategy(resetType);

        //3. 验证
        switch (resetType) {
            case PHONE:
                resetStrategy.verify(command.getPhone(), command.getVerifyCode());
                break;
            case EMAIL:
                resetStrategy.verify(command.getEmail(), command.getVerifyCode());
                break;
        }

        // 4. 更新密码
        authUserRepository.save(AuthUser.builder().password(Password.fromRaw(password)).build());
    }



}

