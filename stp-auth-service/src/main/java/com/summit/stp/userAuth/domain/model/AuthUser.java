package com.summit.stp.userAuth.domain.model;

import com.summit.stp.common.application.domain.model.Password;
import com.summit.stp.common.application.domain.model.PhoneNumber;
import com.summit.stp.common.application.domain.model.Username;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
@EqualsAndHashCode
public class AuthUser {
    private final Long userId;
    private final Username username;
    private  Password password;
    @Setter
    private Integer statusCode;
    private final PhoneNumber phoneNumber;

    public boolean isActive() {
        return statusCode == null || statusCode == 1;
    }
}

