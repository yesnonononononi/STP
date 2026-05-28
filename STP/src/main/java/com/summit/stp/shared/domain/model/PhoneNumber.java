package com.summit.stp.shared.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import com.summit.stp.shared.exception.ParameterException;

@Data
@AllArgsConstructor
public class PhoneNumber {
    private final String value;

    public static PhoneNumber of(String phoneNumber) {
        validate(phoneNumber);
        return new PhoneNumber(phoneNumber);
    }

    public static void validate(String phoneNumber) {
        // 验证手机号码的逻辑
        if (phoneNumber == null || !phoneNumber.matches("^1[3-9]\\d{9}$")) {
            throw new ParameterException("手机号码格式不正确");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
