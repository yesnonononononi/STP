package com.summit.stp.user.domain.model;

import com.summit.stp.common.application.domain.exception.ParameterException;
import com.summit.stp.common.application.domain.model.Password;
import com.summit.stp.common.application.domain.model.PhoneNumber;
import com.summit.stp.common.application.domain.model.Username;
import com.summit.stp.user.domain.exception.UserPasswordErrorException;
import com.summit.stp.user.infrastructure.constants.UserConstants;
import io.netty.util.internal.StringUtil;
import jakarta.annotation.Nullable;
import lombok.*;

import java.sql.Timestamp;
import java.time.Instant;

@Getter
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Setter
    private Long id;
    private Username username;
    private String nick;
    private String avatar;
    private String ip;
    private Email email;
    private Long liked;
    private String bgImage;
    private Long topic;
    private Long fans;
    private String introduction;
    private String vipType;
    private Timestamp vipExpireDate;
    private Password password;
    @Builder.Default
    private Integer statusCode = 1;
    private PhoneNumber phoneNumber;
    private Integer gender;
    private Integer age;
    private Instant createTime;
    private Instant updateTime;

    @Getter
    public enum StatusCode {
        ACTIVE(1),
        BANNED(0);

        private final int code;

        StatusCode(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    public void updateIp(String ip) {
        this.ip = ip;
        this.updateTime = Instant.now();
    }

    public void updateBgImage(String bgImage) {
        if (StringUtil.isNullOrEmpty(bgImage)) {
            throw new ParameterException("背景图片不能为空");
        }
        this.bgImage = bgImage;
        this.updateTime = Instant.now();
    }

    public void clearBgImage() {
        this.bgImage = null;
        this.updateTime = Instant.now();
    }

    public void updateProfile(String nick, String avatar, String email, @Nullable String introduction, @Nullable String verifyCode, Integer gender, Integer age) {
        if (!StringUtil.isNullOrEmpty(nick)) {
            if (nick.length() > UserConstants.Business.MAX_NICK_LENGTH) {
                throw new IllegalArgumentException("昵称长度不能超过" + UserConstants.Business.MAX_NICK_LENGTH + "字");
            }
            this.nick = nick;
        }
        if (!StringUtil.isNullOrEmpty(avatar)) this.avatar = avatar;
        if (introduction != null) {
            if (introduction.length() > UserConstants.Business.MAX_INTRODUCE_LENGTH) {
                throw new IllegalArgumentException("个人简介长度不能超过" + UserConstants.Business.MAX_INTRODUCE_LENGTH + "字");
            }
            this.introduction = introduction;
        }
        if (!StringUtil.isNullOrEmpty(email) && !StringUtil.isNullOrEmpty(verifyCode)) {
            if (this.email == null) {
                this.email = Email.of(email);
            } else {
                this.email.update(email);
            }
        }
        if (gender != null) {
            if (gender != 0 && gender != 1) {
                throw new IllegalArgumentException("无效性别");
            }
            this.gender = gender;
        }
        if (age != null) {
            if (age < 0 || age > 120) {
                throw new IllegalArgumentException("无效年龄");
            }
            this.age = age;
        }
        this.updateTime = Instant.now();
    }

    public void changePassword(String oldRaw, Password newPassword) {
        if (!this.password.matches(oldRaw)) {
            throw new UserPasswordErrorException();
        }
        this.password = newPassword;
        this.updateTime = Instant.now();
    }

    public void changePhoneNumber(PhoneNumber newPhone) {
        this.phoneNumber = newPhone;
        this.updateTime = Instant.now();
    }

    public boolean isActive() {
        return statusCode == StatusCode.ACTIVE.getCode();
    }

    public void toggleBan(boolean attemptBan) {
        this.statusCode = attemptBan ? StatusCode.BANNED.getCode() : StatusCode.ACTIVE.getCode();
        this.updateTime = Instant.now();
    }

    public boolean vipIsExpire(){
        return this.vipExpireDate.before(Timestamp.from(Instant.now()));
    }
}
