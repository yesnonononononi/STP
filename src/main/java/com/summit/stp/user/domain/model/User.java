package com.summit.stp.user.domain.model;

import com.summit.stp.shared.domain.model.Password;
import com.summit.stp.shared.domain.model.PhoneNumber;
import com.summit.stp.shared.domain.model.Username;
import com.summit.stp.userAuth.domain.exception.PasswordErrorException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class User {
    private final Username username; // 标识符，通常不改变
    private Password password;
    private Integer statusCode = 1;
    private PhoneNumber phoneNumber;


    private User(Username username, Password password, PhoneNumber phoneNumber) {
        this.username = username;
        this.password = password;
        this.phoneNumber = phoneNumber;
    }

    public static User create(Username username, Password password, PhoneNumber phoneNumber) {
        if (username == null) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (password == null) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (phoneNumber == null) {
            throw new IllegalArgumentException("手机号不能为空");
        }

        return new User(username, password, phoneNumber);
    }

    /**
     * 修改密码逻辑
     */
    public void changePassword(String oldRaw, Password newPassword) {
        if (!this.verifyPassword(oldRaw)) {
            throw new PasswordErrorException();
        }
        this.password = newPassword;
    }

    /**
     * 修改手机号
     */
    public void changePhoneNumber(PhoneNumber newPhone) {
        this.phoneNumber = newPhone;
    }

    public boolean verifyPassword(String rawPassword) {
        Password inputPassword = Password.fromRaw(rawPassword);
        return this.password.getEncryptedValue().equals(inputPassword.getEncryptedValue());
    }

    public boolean isActive() {
        return statusCode == 1;
    }
}
