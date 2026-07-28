package com.summit.stp.common.application.domain.model;

import io.netty.util.internal.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class UserSession {
    private final Long id;
    private final String username;
    private String ip;
    private final LocalDateTime loginTime;
    private String onlineStatus;
    private final String token;
    private final Integer admin;
    private final TokenType tokenType;

    @Getter
    public enum TokenType{
        ACCESS("access"),
        GUEST("guest"),
        REFRESH("refresh");
        private final String value;
        TokenType(String value){
            this.value = value;
        }

        public static TokenType getByValue(String value){
            for(TokenType tokenType : TokenType.values()){
                if(tokenType.getValue().equals(value)){
                    return tokenType;
                }
            }
            return null;
        }
    }

    public boolean isLogin(){
        return !StringUtil.isNullOrEmpty(token) && !StringUtil.isNullOrEmpty(username) && Objects.equals(tokenType, TokenType.ACCESS);
    }

}
