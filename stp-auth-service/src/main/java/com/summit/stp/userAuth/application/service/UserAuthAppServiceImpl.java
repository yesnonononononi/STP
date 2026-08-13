package com.summit.stp.userAuth.application.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.summit.stp.common.ThreadContext.UserHolder;
import com.summit.stp.common.application.domain.event.UserRegisterEvent;
import com.summit.stp.common.application.domain.exception.BusinessException;
import com.summit.stp.common.application.domain.exception.ParameterException;
import com.summit.stp.common.application.domain.model.Password;
import com.summit.stp.common.application.domain.model.PhoneNumber;
import com.summit.stp.common.application.domain.model.UserSession;
import com.summit.stp.common.application.domain.model.Username;
import com.summit.stp.common.application.domain.service.CaptchaService;
import com.summit.stp.common.constants.UserAuthConstants;
import com.summit.stp.common.feign.UserFeignClient;
import com.summit.stp.common.result.Result;
import com.summit.stp.common.util.IpUtil;
import com.summit.stp.userAuth.application.UserAuthApplicationService;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
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

        UserSession userSession = UserSession.builder()
                .id(user.getId())
                .username(user.getUsername().getValue())
                .ip(command.getIp())
                .loginTime(LocalDateTime.now())
                .onlineStatus("1")
                .tokenType(UserSession.TokenType.ACCESS)
                .build();

        //签发双 Token、管理 Redis 映射、维护 Session
        Map.Entry<String,String> entry = tokenRepository.generateToken(userSession);

        // 通过 Feign 调用 user-service 异步更新用户 IP 地理归属地信息
        try {
            String ipLocation = IpUtil.toString(command.getIp());
            userFeignClient.updateUserIp(user.getId(), ipLocation);
        } catch (Exception e) {
            log.error("【登录】更新用户 IP 地理归属地失败, userId: " + user.getId(), e);
        }

        LoginVO loginVO = LoginVO.builder()
                .token(entry.getKey())
                .refreshToken(entry.getValue())
                .username(user.getUsername().getValue())
                .expireTime( UserAuthConstants.Business.DEFAULT_TOKEN_EXPIRE_SECONDS)
                .build();

        return Result.success(loginVO);
    }

    @Override
    public Result<RefreshTokenVO> refreshToken(RefreshTokenCommand refreshTokenCommand) {
        if(StrUtil.equals(refreshTokenCommand.getRefreshToken(), UserAuthConstants.Business.JWT_FALLBACK,true)){
            return Result.error("令牌无效,请尝试重新登录");
        }
        String username = refreshTokenCommand.getUsername();
        String newAccessToken = IdUtil.fastSimpleUUID();
        String oldAccessToken = refreshTokenCommand.getOldAccessToken();
        String refreshToken = refreshTokenCommand.getRefreshToken();
        log.info("【Token-refresh】用户刷新 Token 请求: {}", username);
        AuthUser user = authUserRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);
        UserSession userSession = UserSession.builder()
                .username(username)
                .token(newAccessToken)
                .ip(refreshTokenCommand.getIp())
                .id(user.getId())
                .loginTime(LocalDateTime.now())
                .onlineStatus("1")
                .tokenType(UserSession.TokenType.ACCESS)
                .build();


        if (!tokenRepository.refreshToken(oldAccessToken,refreshToken,newAccessToken,userSession)) {
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
        tokenRepository.logout(oldAccessToken,oldRefreshToken,user.getUsername());
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

    @Transactional(rollbackFor = Exception.class)
    @Override
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



}
