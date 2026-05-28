package com.summit.stp.shared.domain.model;

import com.summit.stp.shared.constant.UserAuthConstants;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import com.summit.stp.shared.exception.ParameterException;

@Getter
@EqualsAndHashCode
public class Username{
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
        if (value.length() <= UserAuthConstants.MIN_USERNAME_LENGTH) {
            throw new ParameterException(
                    String.format("用户名长度不能小于%d", UserAuthConstants.MIN_USERNAME_LENGTH));
        }
        if (value.length() > UserAuthConstants.MAX_USERNAME_LENGTH) {
            throw new ParameterException(
                    String.format("用户名长度不能大于%d", UserAuthConstants.MAX_USERNAME_LENGTH));
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
