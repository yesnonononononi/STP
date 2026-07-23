package com.summit.stp.shared.domain.service;


public interface CaptchaService {
    public boolean validate(String phoneNumber, Integer verifyCode);
    boolean validateEmail(String email,String verifyCode);
    public void send(String phoneNumber);
    public void sendForEmail(String email);
}
