package com.summit.stp.userAuth.application;

import com.summit.stp.common.result.Result;
import com.summit.stp.userAuth.application.command.ForgetCommand;
import com.summit.stp.userAuth.application.command.LoginCommand;
import com.summit.stp.userAuth.application.command.RefreshTokenCommand;
import com.summit.stp.userAuth.application.command.RegisterCommand;
import com.summit.stp.userAuth.application.vo.LoginVO;
import com.summit.stp.userAuth.application.vo.RefreshTokenVO;
import org.springframework.transaction.annotation.Transactional;

public interface UserAuthApplicationService {

    Result<LoginVO> login(LoginCommand command);

    Result<RefreshTokenVO> refreshToken(RefreshTokenCommand refreshTokenCommand);

    void logout(String oldAccessToken, String oldRefreshToken);

    void register(RegisterCommand command);

    @Transactional
    void forget(ForgetCommand command);
}
