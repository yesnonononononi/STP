package com.summit.stp.shared.service.Captcha;

import com.summit.stp.shared.domain.service.CaptchaService;
import org.springframework.stereotype.Service;

@Service
public class CaptchaServiceImpl implements CaptchaService {
    @Override
    public boolean validate(String phoneNumber, Integer verifyCode) {
        return verifyCode != null && verifyCode == 123456;
    }

    @Override
    public void send(String phoneNumber) {

    }
}
