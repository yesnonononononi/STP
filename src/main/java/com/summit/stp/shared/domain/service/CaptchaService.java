package com.summit.stp.shared.domain.service;


public interface CaptchaService {
    public boolean validate(String phoneNumber, Integer verifyCode);
    public void send(String phoneNumber);
}
