package com.summit.stp.userAuth.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSession {
    private Long id;
    private String username;
    private String ip;
    private LocalDateTime loginTime;
    private String onlineStatus; // e.g., "ONLINE", "OFFLINE"
    private String token;
}
