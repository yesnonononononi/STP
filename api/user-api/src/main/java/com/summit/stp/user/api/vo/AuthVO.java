package com.summit.stp.user.api.vo;

import com.summit.stp.common.application.domain.model.Password;
import com.summit.stp.common.application.domain.model.PhoneNumber;
import com.summit.stp.common.application.domain.model.Username;
import lombok.Builder;
import lombok.Data;


import java.io.Serializable;

@Data
@Builder
public class AuthVO implements Serializable {
    private final Long userId;
    private final Username username;
    private Password password;
    private Integer statusCode;
    private final PhoneNumber phoneNumber;
}
