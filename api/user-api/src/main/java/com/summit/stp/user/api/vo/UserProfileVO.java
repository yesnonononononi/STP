package com.summit.stp.user.api.vo;

import com.summit.stp.common.annotation.PublicId;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * 用户简单信息VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileVO {
    @PublicId
    @ApiModelProperty("用户ID")
    private Long id;
    @ApiModelProperty("用户昵称")
    private String nick;
    @ApiModelProperty("用户头像")
    private String avatar;
    @ApiModelProperty("电话号码")
    private String phone;
    @ApiModelProperty("用户邮箱")
    private String email;
    @ApiModelProperty("会员等级")
    private String memberLevel;
    @ApiModelProperty("用户IP")
    private String ip;
    @ApiModelProperty("用户点赞")
    private String liked;
    @ApiModelProperty("用户话题")
    private String topic;
    @ApiModelProperty("用户粉丝")
    private String fans;
    @ApiModelProperty("会员类型")
    private String vipType;
    @ApiModelProperty("用户简介")
    private String introduction;
    @ApiModelProperty("会员到期时间")
    private String vipExpireDate;
    @ApiModelProperty("会员等级图标")
    private String vipConfigIcon;
    @ApiModelProperty("性别")
    private Integer gender;
    @ApiModelProperty("年龄")
    private Integer age;
    @ApiModelProperty("是否关注")
    private boolean followed;
    @ApiModelProperty("背景图")
    private String bgImage;
    @ApiModelProperty("注册时间")
    private String createTime;
    @ApiModelProperty("状态")
    private String status;

    /**
     * 手机号脱敏：保留前 3 位与后 4 位，中间以 **** 代替（如 138****8888）。
     * <p>幂等：若入参已含 {@code *} 则原样返回，避免重复脱敏破坏数据。</p>
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.isBlank()) return phone;
        if (phone.contains("*")) return phone;
        String value = phone.trim();
        if (value.length() <= 7) return "****";
        return value.substring(0, 3) + "****" + value.substring(value.length() - 4);
    }

    /**
     * 邮箱脱敏：本地部分保留首字符，其余以 **** 代替（如 a****@x.com）。
     * <p>幂等：若入参已含 {@code *} 则原样返回。</p>
     */
    public static String maskEmail(String email) {
        if (email == null || email.isBlank()) return email;
        if (email.contains("*")) return email;
        int at = email.indexOf('@');
        if (at <= 0) return "****";
        String local = email.substring(0, at);
        String domain = email.substring(at);
        if (local.length() <= 1) return "*" + domain;
        return local.charAt(0) + "****" + domain;
    }

    /**
     * 管理端响应脱敏：对 {@code phone} / {@code email} 原地脱敏。
     * <p><b>仅供管理端（Admin）响应调用</b>；C 端链路请勿调用，保持原字段语义，避免影响 C 端展示。</p>
     */
    public void maskSensitiveInfo() {
        this.phone = maskPhone(this.phone);
        this.email = maskEmail(this.email);
    }
}

