package com.summit.stp.user.api.dto.request;

import lombok.Data;

@Data
public class UserPutRequest {
    private String uname;
    private String password;
    private String phoneNumber;
    private Integer statusCode;
}
