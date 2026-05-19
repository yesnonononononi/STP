package com.summit.stp.user.domain.service;
import com.summit.stp.shared.domain.model.PhoneNumber;
import com.summit.stp.shared.domain.service.CaptchaService;
import com.summit.stp.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDomainService {
    private final CaptchaService captchaService;

    /**
     * 协调手机号修改逻辑（涉及外部验证码校验）
     */
    public void changePhone(User user, PhoneNumber newPhone, Integer verifyCode) {
        // 1. 校验验证码
        captchaService.validate(newPhone.getValue(), verifyCode);
        
        // 2. 修改实体状态
        user.changePhoneNumber(newPhone);
    }
}
