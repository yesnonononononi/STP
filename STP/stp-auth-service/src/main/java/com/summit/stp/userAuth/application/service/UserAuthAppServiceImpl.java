package com.summit.stp.userAuth.application.service;

import cn.hutool.core.util.IdUtil;
import com.summit.stp.shared.ThreadContext.UserHolder;
import com.summit.stp.shared.constants.UserAuthConstants;
import com.summit.stp.shared.domain.model.Password;
import com.summit.stp.shared.domain.model.PhoneNumber;
import com.summit.stp.shared.domain.model.Username;
import com.summit.stp.shared.domain.service.CaptchaService;
import com.summit.stp.shared.exception.ParameterException;
import com.summit.stp.shared.result.Result;
import com.summit.stp.shared.util.IpUtil;
import com.summit.stp.common.feign.UserFeignClient;
import com.summit.stp.userAuth.application.UserAuthApplicationService;
import com.summit.stp.userAuth.application.command.ForgetCommand;
import com.summit.stp.userAuth.application.command.LoginCommand;
import com.summit.stp.userAuth.application.command.RefreshTokenCommand;
import com.summit.stp.userAuth.application.command.RegisterCommand;
import com.summit.stp.userAuth.application.vo.LoginVO;
import com.summit.stp.userAuth.application.vo.RefreshTokenVO;
import com.summit.stp.shared.domain.event.UserRegisterEvent;
import com.summit.stp.userAuth.domain.exception.RefreshTokenNoValidException;
import com.summit.stp.userAuth.domain.exception.ResetPasswordException;
import com.summit.stp.userAuth.domain.model.AuthToken;
import com.summit.stp.userAuth.domain.model.AuthUser;
import com.summit.stp.userAuth.domain.model.ResetType;
import com.summit.stp.userAuth.domain.model.UserSession;
import com.summit.stp.userAuth.domain.repository.AuthUserRepository;
import com.summit.stp.userAuth.domain.repository.TokenRepository;
import com.summit.stp.userAuth.domain.service.ResetPasswordStrategy;
import com.summit.stp.userAuth.domain.service.ResetStrategyRegistry;
import com.summit.stp.userAuth.domain.service.UserAuthDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAuthAppServiceImpl implements UserAuthApplicationService {
    private final UserAuthDomainService userAuthDomainService;
    private final ResetStrategyRegistry resetStrategyRegistry;
    private final AuthUserRepository authUserRepository;
    private final TokenRepository tokenRepository;
    private final CaptchaService captchaService;
    private final PlatformTransactionManager transactionManager;
    private final UserRegisterEventPublishProvider userRegisterEventPublishProvider;
    private final UserFeignClient userFeignClient;


    @Override
    public Result<LoginVO> login(LoginCommand command) {
        // 核心密码与状态激活的身份核对
        AuthUser user = userAuthDomainService.authenticate(
                command.getUsername(),
                command.getPassword()
        );

        //签发双 Token、管理 Redis 映射、维护 Session
        AuthToken authToken = issueToken(
                user.getUsername().getValue(),
                command.getIp(),

                user.getId()
        );

        // 通过 Feign 调用 user-service 异步更新用户 IP 地理归属地信息
        try {
            String ipLocation = IpUtil.toString(command.getIp());
            userFeignClient.updateUserIp(user.getId(), ipLocation);
        } catch (Exception e) {
            log.error("【登录】更新用户 IP 地理归属地失败, userId: " + user.getId(), e);
        }

        LoginVO loginVO = LoginVO.builder()
                .token(authToken.getAccessToken())
                .refreshToken(authToken.getRefreshToken())
                .username(user.getUsername().getValue())
                .expireTime(UserAuthConstants.DEFAULT_TOKEN_EXPIRE_SECONDS)
                .build();

        return Result.success(loginVO);
    }

    @Override
    public Result<RefreshTokenVO> refreshToken(RefreshTokenCommand refreshTokenCommand) {
        String username = refreshTokenCommand.getUsername();
        log.info("【Token-refresh】用户刷新 Token 请求: {}", username);

        // 1. 根据 Token 检索 Session
        UserSession session = tokenRepository.findSessionByToken(refreshTokenCommand.getRefreshToken(), UserSession.TokenType.REFRESH)
                .orElseThrow(RefreshTokenNoValidException::new);

        // 2. 校验 Token 类型是否为 REFRESH
        if (!UserSession.TokenType.REFRESH.equals(session.getTokenType())) {
            throw new RefreshTokenNoValidException();
        }

        // 3. 安全的一致性比对校验
        userAuthDomainService.verifySessionUsername(session.getUsername(), username);

        // 4. 签发新 Access Token 与 Refresh Token 并更新
        AuthToken authToken = issueToken(
                username,
                refreshTokenCommand.getIp(),
                session.getId()
        );

        RefreshTokenVO refreshTokenVO = RefreshTokenVO.builder()
                .token(authToken.getAccessToken())
                .refreshToken(authToken.getRefreshToken())
                .build();
        log.info("【Token-refresh】 success :{}", username);
        return Result.success(refreshTokenVO);
    }

    @Override
    public void logout() {
        // 登出操作的流程编排 (清理 Token 以及 Session 缓存)
        UserSession user = UserHolder.getUser();
        String value = user.getUsername();
        tokenRepository.deleteByUsername(value, UserSession.TokenType.ACCESS);
        tokenRepository.deleteByUsername(value, UserSession.TokenType.REFRESH);
        String token = user.getToken();
        tokenRepository.deleteByToken(token, UserSession.TokenType.ACCESS);
    }

    @Override
    public void register(RegisterCommand command) {
        log.info("用户注册请求: {}", command.getPhoneNumber());
        
        // 1. 校验验证码
        resetStrategyRegistry.getResetStrategy(ResetType.PHONE).verify(command.getPhoneNumber(), command.getVerifyCode());
        // 2. 校验手机号是否已注册
        if (authUserRepository.existsByPhone(command.getPhoneNumber())) {
            throw new ParameterException("该手机号已被注册");
        }
        // 3. 创建 AuthUser 并持久化 (身份信息)
        String username = "U_" + command.getPhoneNumber();
        AuthUser authUser = AuthUser.builder()
                .username(Username.of(username))
                .password(Password.fromRaw(command.getPassword()))
                .phoneNumber(PhoneNumber.of(command.getPhoneNumber()))
                .build();

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.executeWithoutResult(status -> {
            authUserRepository.save(authUser);
        });

        // 4. 发布领域事件，触发跨模块的 User 创建个人档案 (松耦合设计)
        userRegisterEventPublishProvider.publish(new UserRegisterEvent(command.getPhoneNumber(), username, authUser.getPassword().getEncryptedValue()));
        
        log.info("用户注册成功: {}", command.getPhoneNumber());
    }

    @Override
    @Transactional
    public void forget(ForgetCommand command) {
        // 1. 策略安全验证与降级
        Integer resetCode = command.getResetType();
        ResetType resetType;
        if (resetCode == null || (resetType = ResetType.fromCode(resetCode)) == null) {
            throw new ResetPasswordException("无效的找回方式");
        }
        // 2. 获取并分发对应策略进行凭证校验
        ResetPasswordStrategy resetStrategy = resetStrategyRegistry.getResetStrategy(resetType);
        
        Optional<AuthUser> userOpt = Optional.empty();
        switch (resetType) {
            case PHONE:
                resetStrategy.verify(command.getPhone(), command.getVerifyCode());
                userOpt = authUserRepository.findByPhone(command.getPhone());
                break;
            case EMAIL:
                resetStrategy.verify(command.getEmail(), command.getVerifyCode());
                break;
        }

        // 3. 用户存在性验证
        if (userOpt.isEmpty()) {
            throw new ResetPasswordException("用户不存在");
        }

        // 4. 更新密码 (自动采用 BCrypt 加密并在持久化时写入)
        AuthUser user = userOpt.get();
        user.updatePassword(Password.fromRaw(command.getPassword()));
        authUserRepository.save(user);
    }

    /**
     * 签发双 Token
     */
    private AuthToken issueToken(String username, String ip, Long userId) {
        String refreshToken = "", accessToken;
        // 删除旧 Session
        tokenRepository.deleteByUsername(username, UserSession.TokenType.ACCESS);

        // 获取 accessToken
        accessToken = acquireAccessToken(username, username, ip,UserAuthConstants.DEFAULT_TOKEN_EXPIRE_SECONDS,userId, UserSession.TokenType.ACCESS,  UserSession.TokenType.ACCESS);

        // 如果需要刷新令牌,则删除原有映射,使 token 失效
        tokenRepository.deleteByUsername(username, UserSession.TokenType.REFRESH);
        // 获取刷新令牌
        refreshToken = acquireAccessToken(username, username, ip, UserAuthConstants.DEFAULT_REFRESH_TOKEN_EXPIRE_SECONDS,userId, UserSession.TokenType.REFRESH,  UserSession.TokenType.REFRESH);
        return AuthToken.builder().accessToken(accessToken).refreshToken(refreshToken).build();
    }

    private String acquireAccessToken(String cacheKey, String realUsername, String ip, long expireSeconds,Long userId,  UserSession.TokenType  tokenType,  UserSession.TokenType  type) {
        // 生成新 Token
        String accessToken = IdUtil.fastSimpleUUID();

        // 创建 Session 信息
        UserSession session = UserSession.builder()
                .id(userId)
                .username(realUsername) // 存储真实用户名，防止校验时出现前缀不匹配问题
                .ip(ip)
                .loginTime(LocalDateTime.now())
                .onlineStatus("ONLINE")
                .token(accessToken)
                .tokenType(tokenType)
                .build();

        // 保存映射
        // 1. token -> session
        tokenRepository.saveSession(accessToken, session, expireSeconds, type);

        // 2. cacheKey -> token (用于后续查找)
        tokenRepository.saveUserToken(cacheKey, accessToken, expireSeconds, type);

        return accessToken;
    }
}
