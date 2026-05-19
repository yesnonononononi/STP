package com.summit.stp.shared.constant;

public class UserAuthConstants {
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_PASSWORD_LENGTH = 20;
    public static final int MAX_USERNAME_LENGTH = 10;
    public static final int MIN_USERNAME_LENGTH = 1;
    public static final long DEFAULT_TOKEN_EXPIRE_SECONDS = 3600 * 2; // 2小时
    public static final String TOKEN_CACHE_PREFIX = "auth:token:";
    public static final String SESSION_CACHE_PREFIX = "auth:session:";
    public static final String REFRESH_TOKEN_PREFIX = "refresh-token:";

    private UserAuthConstants() {
    }
}
