package com.summit.stp.user.application.command;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserPhoneBindCommand {
    private final String phoneNumber;
    private final Integer verifyCode;
}
