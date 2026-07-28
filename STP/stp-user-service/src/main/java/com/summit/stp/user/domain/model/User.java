package com.summit.stp.user.domain.model;

import com.summit.stp.common.application.domain.model.Password;
import com.summit.stp.common.application.domain.model.PhoneNumber;
import com.summit.stp.common.application.domain.model.Username;
import com.summit.stp.common.application.domain.exception.ParameterException;
import com.summit.stp.user.domain.exception.UserPasswordErrorException;
import io.netty.util.internal.StringUtil;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.sql.Timestamp;
import com.summit.stp.user.infrastructure.constants.UserConstants;

@Getter
@EqualsAndHashCode
@Builder
public class User {
    private final Long id;
    private final Username username;
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

    /**
     * 更新用户登录地理IP位置
     */
    public void updateIp(String ip) {
        this.ip = ip;
    }

    public void updateBgImage(String bgImage) {
        if(StringUtil.isNullOrEmpty(bgImage)){
            throw new ParameterException("背景图片不能为空");
        }
        this.bgImage = bgImage;
    }

    public void clearBgImage() {
        this.bgImage = null;
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
            if(gender != 0 && gender != 1){
                throw new IllegalArgumentException("无效性别");
            }
            this.gender = gender;
        }
        if (age != null) {
            if(age < 0 || age > 120){
                throw new IllegalArgumentException("无效年龄");
            }
            this.age = age;
        }
    }


    /**
     * 修改密码逻辑
     */
    public void changePassword(String oldRaw, Password newPassword) {
        if (!this.password.matches(oldRaw)) {
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




    public boolean isActive() {
        return statusCode == 1;
    }
}
