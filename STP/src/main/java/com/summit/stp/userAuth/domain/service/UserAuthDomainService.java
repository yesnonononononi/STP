package com.summit.stp.userAuth.domain.service;

import cn.hutool.core.util.IdUtil;
import com.summit.stp.shared.ThreadContext.UserHolder;
import com.summit.stp.shared.constant.UserAuthConstants;
import com.summit.stp.shared.domain.model.Password;
import com.summit.stp.shared.domain.model.PhoneNumber;
import com.summit.stp.shared.domain.model.Username;
import com.summit.stp.shared.domain.service.CaptchaService;
import com.summit.stp.userAuth.domain.event.UserRegisterEvent;
import com.summit.stp.userAuth.domain.exception.PasswordErrorException;
import com.summit.stp.userAuth.domain.exception.RefreshTokenNoValidException;
import com.summit.stp.userAuth.domain.exception.RefuseProvidingTokenException;
import com.summit.stp.userAuth.domain.exception.UserNotFoundException;
import com.summit.stp.userAuth.domain.model.AuthToken;
import com.summit.stp.userAuth.domain.model.AuthUser;
import com.summit.stp.userAuth.domain.model.UserSession;
import com.summit.stp.userAuth.domain.repository.AuthUserRepository;
import com.summit.stp.userAuth.domain.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class UserAuthDomainService {
    private final AuthUserRepository authUserRepository;
    private final TokenRepository tokenRepository;
    private final CaptchaService captchaService;
    private final ApplicationEventPublisher eventPublisher;

    public AuthUser authenticate(String username, String rawPassword) {
        AuthUser user = authUserRepository.findByUsername(username)
                .orElseThrow(UserNotFoundException::new);

        if (!user.getPassword().matches(rawPassword)) {
            throw new PasswordErrorException();
        }
        if (!user.isActive()) {
            throw new RefuseProvidingTokenException(username, "用户已被封禁");
        }
        return user;
    }

    public void isValid(String refreshTokenValue, String username) {
        // 先根据 Token 查 Session
        UserSession session = tokenRepository.findSessionByToken(refreshTokenValue)
                .orElseThrow(RefreshTokenNoValidException::new);

        // 校验用户名是否匹配
        if (!session.getUsername().equals(username)) {
            throw new RefreshTokenNoValidException();
        }
    }


    /**
     * 签发双 Token (登录时调用)
     * @param username 用户名
     * @param ip ip
     * @param expireSeconds Access Token 过期时间
     * @return AuthToken
     */
    public AuthToken issueToken(String username, String ip, long expireSeconds, boolean requireRefreshToken) {
        String refreshToken = "", accessToken;
        // 删除旧 Session
        tokenRepository.deleteByUsername(username);

        //获取accessToken
        accessToken = acquireAccessToken(username, username, ip, expireSeconds);

        //如果需要刷新令牌,则删除原有映射,使token失效
        if (requireRefreshToken) {
            tokenRepository.deleteByUsername(UserAuthConstants.REFRESH_TOKEN_PREFIX + username);
            //获取刷新令牌 (缓存键为带前缀的名称，但 Session 内存储的仍为真实用户名)
            refreshToken = acquireAccessToken(UserAuthConstants.REFRESH_TOKEN_PREFIX + username, username, ip, expireSeconds * 2);
        }
        return new AuthToken(accessToken, refreshToken);
    }

    private String acquireAccessToken(String cacheKey, String realUsername, String ip, long expireSeconds){
        // 生成新 Token
        String accessToken = IdUtil.fastSimpleUUID();

        // 创建 Session 信息
        UserSession session = UserSession.builder()
                .username(realUsername) // 存储真实用户名，防止校验时出现前缀不匹配问题
                .ip(ip)
                .loginTime(LocalDateTime.now())
                .onlineStatus("ONLINE")
                .token(accessToken)
                .build();

        // 保存映射
        // 1. token -> session
        tokenRepository.saveSession(accessToken, session, expireSeconds);

        // 2. cacheKey -> token (用于后续查找)
        tokenRepository.saveUserToken(cacheKey, accessToken, expireSeconds);

        return accessToken;
    }


    public void logout() {
        UserSession user = UserHolder.getUser();
        String value = user.getUsername();
        tokenRepository.deleteByUsername(value);
        tokenRepository.deleteByUsername(UserAuthConstants.REFRESH_TOKEN_PREFIX + value);
        String token = user.getToken();
        tokenRepository.deleteByToken(token);
    }

    /**
     * 注册逻辑
     */
    public void register(String phoneNumber, String password, String code) {
        // 1. 校验验证码
        captchaService.validate(phoneNumber, Integer.valueOf(code));

        // 2. 校验手机号是否已注册
        if (authUserRepository.existsByPhone(phoneNumber)) {
            throw new IllegalArgumentException("该手机号已被注册");
        }

        // 3. 创建 AuthUser (身份信息)
        String username = "U_" + phoneNumber;
        AuthUser authUser = AuthUser.create(
                Username.of(username),
                Password.fromRaw(password),
                PhoneNumber.of(phoneNumber));
        authUserRepository.save(authUser);

        // 4. 发布领域事件，触发 User 模块创建个人档案（传递哈希后的密码）
        eventPublisher.publishEvent(new UserRegisterEvent(phoneNumber, username, authUser.getPassword().getEncryptedValue()));
    }
}
