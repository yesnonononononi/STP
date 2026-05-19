package com.summit.stp.userAuth.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class AuthToken {
    private final String accessToken;
    private final String refreshToken;

}
