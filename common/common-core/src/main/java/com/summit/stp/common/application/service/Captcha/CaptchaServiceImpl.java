package com.summit.stp.common.application.service.Captcha;

import com.summit.stp.common.application.domain.service.CaptchaService;
import org.springframework.stereotype.Service;

@Service
public class CaptchaServiceImpl implements CaptchaService {
    @Override
    public boolean validate(String phoneNumber, Integer verifyCode) {
        return verifyCode != null && verifyCode == 123456;
    }

    @Override
    public boolean validateEmail(String email, String verifyCode) {
        return false;
    }

    @Override
    public void send(String phoneNumber) {

    }

    @Override
    public void sendForEmail(String email) {

    }
}
