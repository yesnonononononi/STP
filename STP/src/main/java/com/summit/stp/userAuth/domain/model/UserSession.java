package com.summit.stp.userAuth.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    private final String tokenType;
    public void updateIp(String newIp){
        ip = newIp;
    }
    public void updateOnlineStatus(String newStatus){
        onlineStatus = newStatus;
    }

}
