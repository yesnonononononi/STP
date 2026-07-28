package com.summit.stp.userAuth.domain.service;

import com.summit.stp.userAuth.domain.exception.PasswordErrorException;
import com.summit.stp.userAuth.domain.exception.RefreshTokenNoValidException;
import com.summit.stp.userAuth.domain.exception.RefuseProvidingTokenException;
import com.summit.stp.userAuth.domain.exception.UserNotFoundException;
import com.summit.stp.userAuth.domain.model.AuthUser;
import com.summit.stp.userAuth.domain.repository.AuthUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserAuthDomainService {
    private final AuthUserRepository authUserRepository;

    /**
     * 核心身份认证校验逻辑 (复杂业务逻辑)
     */
    public AuthUser authenticate(String nick, String rawPassword) {
        AuthUser user;
        //如果类似于手机号,则优先查询手机号
        if (nick.matches("1[3-9]\\d{9}")) {
            user = authUserRepository.findByPhone(nick)
                    .orElseThrow(UserNotFoundException::new);
        } else {
            user = authUserRepository.findByUsername(nick)
                    .orElseThrow(UserNotFoundException::new);
        }
        if (!user.getPassword().matches(rawPassword)) {
            throw new PasswordErrorException();
        }
        if (!user.isActive()) {
            throw new RefuseProvidingTokenException(nick, "用户已被封禁");
        }
        return user;
    }

    /**
     * 校验 Session 内用户名与请求用户名匹配 (领域校验规则)
     */
    public void verifySessionUsername(String sessionUsername, String requestUsername) {
        if (sessionUsername == null || !sessionUsername.equals(requestUsername)) {
            throw new RefreshTokenNoValidException();
        }
    }
}
