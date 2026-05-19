package com.summit.stp.userAuth.api.dto.request;

import lombok.Data;

@Data
public class RegisterRequest {
    private String phoneNumber;
    private String password;
    private String verifyCode;
}
