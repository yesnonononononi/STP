package com.summit.stp.userAuth.domain.model;

import com.summit.stp.shared.domain.model.Password;
import com.summit.stp.shared.domain.model.PhoneNumber;
import com.summit.stp.shared.domain.model.Username;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@EqualsAndHashCode
public class AuthUser {
    private final Username username;
    private final Password password;
    @Setter
    private Integer statusCode;
    private final PhoneNumber phoneNumber;


    private AuthUser(Username username, Password password, PhoneNumber phoneNumber) {
        this.username = username;
        this.password = password;
        this.phoneNumber = phoneNumber;
    }

    public static AuthUser create(Username username, Password password, PhoneNumber phoneNumber) {
        if (username == null) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (password == null) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (phoneNumber == null) {
            throw new IllegalArgumentException("手机号不能为空");
        }

        return new AuthUser(username, password, phoneNumber);
    }

    public String getUsernameValue() {
        return username.getValue();
    }

    public boolean isActive() {
        return statusCode == null || statusCode == 1;
    }
}

