package com.summit.stp.common.constants;

public interface UserAuthConstants {
    interface Business {
        long GUEST_TOKEN_TTL = 6L * 60 * 60 * 1000;
        int MIN_PASSWORD_LENGTH = 6;
        int MAX_PASSWORD_LENGTH = 20;
        int MAX_USERNAME_LENGTH = 50;
        int MIN_USERNAME_LENGTH = 1;
        long DEFAULT_TOKEN_EXPIRE_SECONDS = 3600 * 2; // 2小时
        long DEFAULT_REFRESH_TOKEN_EXPIRE_SECONDS = 3600 * 24 * 7; // 7天
        String JWT_FALLBACK = "JWT_FALLBACK";
    }

    interface Cache {
        String PREFIX = "user:auth:";
        String ACCESS_TOKEN = PREFIX + "token:access:";
        String REFRESH_TOKEN = PREFIX + "token:refresh:";
        String GUEST_TOKEN = PREFIX + "token:guest:";
        String ACCESS_SESSION = PREFIX + "session:access:";
        String REFRESH_SESSION = PREFIX + "session:refresh:";
        String GUEST_SESSION = PREFIX + "session:guest:";
    }
}
