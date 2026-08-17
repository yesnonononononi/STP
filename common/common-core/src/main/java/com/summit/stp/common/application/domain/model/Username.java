package com.summit.stp.common.application.domain.model;



import com.summit.stp.common.application.domain.exception.ParameterException;
import com.summit.stp.common.constants.UserAuthConstants;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;

@Getter
@EqualsAndHashCode
public class Username implements Serializable {
    private final String value;

    private Username(String value) {
        this.value = value;
    }

    public static Username of(String value) {
        validate(value);
        return new Username(value);
    }


    private static void validate(String value){
        if (value == null || value.trim().isEmpty()) {
            throw new ParameterException("用户名不能为空");
        }
        if (value.length() <= UserAuthConstants.Business.MIN_USERNAME_LENGTH) {
            throw new ParameterException(
                    String.format("用户名长度不能小于%d", UserAuthConstants.Business.MIN_USERNAME_LENGTH));
        }
        if (value.length() > UserAuthConstants.Business.MAX_USERNAME_LENGTH) {
            throw new ParameterException(
                    String.format("用户名长度不能大于%d", UserAuthConstants.Business.MAX_USERNAME_LENGTH));
        }
        if (!value.matches("^[a-zA-Z0-9_]+$")) {
            throw new ParameterException("用户名只能包含字母、数字和下划线");
        }
    }

    @Override
    public String toString() {
        return value;
    }


}
