package com.summit.stp.shared.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;

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
        if (!phoneNumber.matches("^1[3-9]\\d{8}$")) {
            throw new IllegalArgumentException("手机号码格式不正确");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
