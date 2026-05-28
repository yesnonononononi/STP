package com.summit.stp.user.domain.model;

import cn.hutool.core.util.StrUtil;
import io.netty.util.internal.StringUtil;
import lombok.*;


import com.summit.stp.shared.exception.ParameterException;

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
