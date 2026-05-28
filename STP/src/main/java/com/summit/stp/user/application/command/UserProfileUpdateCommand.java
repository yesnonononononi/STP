package com.summit.stp.user.application.command;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserProfileUpdateCommand {
    private final String nick;
    private final String avatar;
    private final String email;
    private final String verifyCode;
    private final String introduce;
    private final Integer gender;
    private final Integer age;
}
