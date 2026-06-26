package com.summit.stp.userAuth.infrastructure.config;

import com.summit.stp.shared.constants.UserAuthConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "stp.auth")
public class AuthProperties {
    /**
     * Token 有效期（秒）
     */
    private long tokenExpireSeconds = UserAuthConstants.DEFAULT_TOKEN_EXPIRE_SECONDS;

    private long refreshTokenExpireSeconds = UserAuthConstants.DEFAULT_REFRESH_TOKEN_EXPIRE_SECONDS;

    /**
     * 是否允许单账号多端登录
     */
    private boolean multiLogin = false;
}
