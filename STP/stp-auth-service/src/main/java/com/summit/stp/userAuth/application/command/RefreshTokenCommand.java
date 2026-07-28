package com.summit.stp.userAuth.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenCommand {
    private String refreshToken;
    private String username;
    private String ip;
}
