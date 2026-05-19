package com.summit.stp.user.application.command;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserPutCommand {
    private final String uname;
    private final String newPassword;
    private final String phoneNumber;
    private final Integer statusCode;
    private final Integer verifyCode;
    private final String oldPassword;
}
