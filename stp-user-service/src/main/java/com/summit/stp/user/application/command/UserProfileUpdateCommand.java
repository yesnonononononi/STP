package com.summit.stp.user.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileUpdateCommand {
    private String nick;
    private String avatar;
    private String email;
    private String verifyCode;
    private String introduce;
    private Integer gender;
    private String bgImage;
    private Integer age;
}
