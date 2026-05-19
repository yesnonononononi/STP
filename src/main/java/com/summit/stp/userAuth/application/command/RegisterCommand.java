package com.summit.stp.userAuth.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterCommand {
    private String phoneNumber;
    private String password;
    private String verifyCode;
}
