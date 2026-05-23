package com.summit.stp.user.domain.model;

import com.summit.stp.shared.domain.model.Password;
import com.summit.stp.shared.domain.model.PhoneNumber;
import com.summit.stp.shared.domain.model.Username;
import com.summit.stp.user.domain.exception.UserPasswordErrorException;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@Builder
public class User {
    private final Username username; // 标识符，通常不改变
    private Password password;
    @Builder.Default
    private Integer statusCode = 1;
    private PhoneNumber phoneNumber;




    /**
     * 修改密码逻辑
     */
    public void changePassword(String oldRaw, Password newPassword) {
        if (!this.verifyPassword(oldRaw)) {
            throw new UserPasswordErrorException();
        }
        this.password = newPassword;
    }

    /**
     * 修改手机号
     */
    public void changePhoneNumber(PhoneNumber newPhone) {
        this.phoneNumber = newPhone;
    }


    /**
     * 验证密码
     * @param rawPassword 原始密码
     * @return 是否验证通过
     */
    public boolean verifyPassword(String rawPassword) {
        Password inputPassword = Password.fromRaw(rawPassword);
        return this.password.getEncryptedValue().equals(inputPassword.getEncryptedValue());
    }

    public boolean isActive() {
        return statusCode == 1;
    }
}
