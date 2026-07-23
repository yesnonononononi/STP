package com.summit.stp.userAuth.domain.service;

import com.summit.stp.shared.domain.service.CaptchaService;
import com.summit.stp.userAuth.domain.exception.ResetPasswordException;
import com.summit.stp.userAuth.domain.model.ResetType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PhoneResetStrategy implements ResetPasswordStrategy{
    private final ResetType type = ResetType.PHONE;
    private final CaptchaService captchaService;

    @Override
    public ResetType getStrategy() {
        return type;
    }

    @Override
    public void verify(String phone, String verifyCode){
        if (phone == null || phone.trim().isEmpty() || verifyCode == null || verifyCode.trim().isEmpty()) {
            throw new ResetPasswordException("手机号和验证码不能为空");
        }
        int code;
        try {
            code = Integer.parseInt(verifyCode);
        } catch (NumberFormatException e) {
            throw new ResetPasswordException("验证码格式错误");
        }
        boolean isValid = captchaService.validate(phone, code);
        if (!isValid) {
            throw new ResetPasswordException("验证码错误或已过期");
        }
    }
}
