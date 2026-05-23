package com.summit.stp.userAuth.domain.model;

import lombok.*;

@Getter
@Builder
public class AuthToken {
    private final String accessToken;
    private final String refreshToken;

}
