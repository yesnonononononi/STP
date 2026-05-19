package com.summit.stp.shared.infrastructure.Captcha;

import com.summit.stp.shared.domain.service.CaptchaService;
import org.springframework.stereotype.Service;

@Service
public class CaptchaServiceImpl implements CaptchaService {
    @Override
    public boolean validate(String phoneNumber, Integer verifyCode) {
        return false;
    }

    @Override
    public void send(String phoneNumber) {

    }
}
