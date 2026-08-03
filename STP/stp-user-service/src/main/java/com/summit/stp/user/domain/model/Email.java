package com.summit.stp.user.domain.model;

import com.summit.stp.common.application.domain.exception.ParameterException;
import io.netty.util.internal.StringUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
@Getter
public class Email {
    private String value;

    private Email(String value) {
        this.value = value;
    }

    public static Email of(String value){
        return new Email(value);
    }

    public void update(String value){
        if(StringUtil.isNullOrEmpty(value)){
            throw new ParameterException("邮箱不能为空");
        } else if (!value.matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$")) {
            throw new ParameterException("邮箱格式不正确");
        }
        this.value = value;
    }
}
