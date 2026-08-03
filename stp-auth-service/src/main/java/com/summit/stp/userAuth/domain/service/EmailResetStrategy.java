package com.summit.stp.userAuth.domain.service;

import com.summit.stp.userAuth.domain.exception.ResetPasswordException;
import com.summit.stp.userAuth.domain.model.ResetType;
import org.springframework.stereotype.Component;

@Component
public class EmailResetStrategy implements ResetPasswordStrategy{
    private final ResetType type = ResetType.EMAIL;
    @Override
    public ResetType getStrategy() {
        return type;
    }

    @Override
    public void verify(String email, String verifyCode){
        throw new ResetPasswordException("目前系统未开启电子邮箱找回服务，请联系管理员");
    }
}
