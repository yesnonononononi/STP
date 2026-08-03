package com.summit.stp.user.application.command;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserPasswordUpdateCommand {
    private final String oldPassword;
    private final String newPassword;
}
