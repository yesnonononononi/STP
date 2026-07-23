package com.summit.stp.shared.constants;

public class UserAuthConstants {
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_PASSWORD_LENGTH = 20;
    public static final int MAX_USERNAME_LENGTH = 50;
    public static final int MIN_USERNAME_LENGTH = 1;
    public static final long DEFAULT_TOKEN_EXPIRE_SECONDS = 3600 * 2; // 2小时
    public static final long DEFAULT_REFRESH_TOKEN_EXPIRE_SECONDS = 3600 * 24 * 7; // 7天

    private UserAuthConstants() {
    }
}
