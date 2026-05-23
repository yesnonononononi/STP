package com.summit.stp.userAuth.application.command;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.sql.Timestamp;
@Builder
@Getter
public class ForgetCommand {
    private String phone;
    private String email;
    private String username;
    private String verifyCode;
    private String password;
    private Timestamp commitTime;
    private Integer resetType;
}
