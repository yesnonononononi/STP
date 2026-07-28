package com.summit.stp.user.infrastructure.constants;

public interface UserConstants {
    interface Business {
        int MAX_INTRODUCE_LENGTH = 100;
        int MAX_NICK_LENGTH = 20;
        long CACHE_TTL = 7L;
    }

    interface Cache {
        String USER_PREFIX = "user:";
        String USER_DETAIL = USER_PREFIX + "detail:";
        
        String MEMBER_PREFIX = "member:";
        String LEVEL_CONFIG = MEMBER_PREFIX + "level:config";
        String PAY_CONSUME_MARK = MEMBER_PREFIX + "pay:consume:";
        String LOCK_PAY = MEMBER_PREFIX + "pay:lock:";
        String STATE_CACHE = MEMBER_PREFIX + "state:";
    }
}
