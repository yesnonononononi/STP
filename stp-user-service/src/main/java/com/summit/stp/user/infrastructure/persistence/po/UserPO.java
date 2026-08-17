package com.summit.stp.user.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import com.summit.stp.common.application.domain.model.Password;
import com.summit.stp.common.application.domain.model.PhoneNumber;
import com.summit.stp.common.application.domain.model.Username;
import com.summit.stp.user.domain.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("user")
public class UserPO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String uname;
    private String nick;
    private String password;
    private String phone;
    private String ip;
    private String email;
    private String avatar;
    private String introduction;
    private Integer statusCode;
    private Integer gender;
    private Integer admin;
    @TableField(value = "bg_image")
    private String bgImage;
    private Integer age;
    private Instant createTime;
    private Instant updateTime;

    public static UserPO toPO(User user) {
        if (user == null) return null;
        return UserPO.builder()
                .id(user.getId())
                .nick(user.getNick())
                .uname(user.getUsername() != null ? user.getUsername().getValue() : null)
                .password(user.getPassword() != null ? user.getPassword().getEncryptedValue() : null)
                .phone(user.getPhoneNumber() != null ? user.getPhoneNumber().getValue() : null)
                .statusCode(user.getStatusCode())
                .avatar(user.getAvatar())
                .introduction(user.getIntroduction())
                .ip(user.getIp())
                .gender(user.getGender())
                .bgImage(user.getBgImage())
                .age(user.getAge())
                .createTime(user.getCreateTime())
                .updateTime(user.getUpdateTime())
                .build();
    }

    public static User toDomain(UserPO po, Long fans, Long topic, Long liked) {
        if (po == null) return null;
        return User.builder()
                .id(po.getId())
                .username(po.getUname() != null ? Username.of(po.getUname()) : null)
                .password(po.getPassword() != null ? Password.fromHash(po.getPassword()) : null)
                .introduction(po.getIntroduction())
                .phoneNumber(po.getPhone() != null ? PhoneNumber.of(po.getPhone()) : null)
                .statusCode(po.getStatusCode())
                .avatar(po.getAvatar())
                .ip(po.getIp())
                .bgImage(po.getBgImage())
                .gender(po.getGender())
                .age(po.getAge())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .fans(Objects.requireNonNullElse(fans, 0L))
                .topic(Objects.requireNonNullElse(topic, 0L))
                .liked(Objects.requireNonNullElse(liked, 0L))
                .build();
    }
}
