package com.summit.stp.userAuth.infrastructure.constants;

public interface UserAuthConstants {
    interface Business {
        long GUEST_TOKEN_TTL = 6L * 60 * 60 * 1000;
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
