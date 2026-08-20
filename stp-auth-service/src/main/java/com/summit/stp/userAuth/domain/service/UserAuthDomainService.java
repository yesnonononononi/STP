package com.summit.stp.userAuth.domain.service;

import cn.hutool.core.util.PhoneUtil;
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
    public AuthUser authenticate(String uname, String rawPassword) {
        ;
        //如果类似于手机号,则优先查询手机号
        AuthUser user = PhoneUtil.isMobile(uname)
                ? authUserRepository.findUserByOrThrow(null,null,uname,UserNotFoundException::new)
                : authUserRepository.findUserByOrThrow(uname,null,null,UserNotFoundException::new);
        if (!user.getPassword().matches(rawPassword)) {
            throw new PasswordErrorException();
        }
        if (!user.isActive()) {
            throw new RefuseProvidingTokenException(uname, "用户已被封禁");
        }
        return user;
    }


}
