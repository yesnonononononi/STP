package com.summit.stp.toolbox.infrastructure.constants;

public interface ToolboxConstants {
    interface Business {
        long TTL = 7;
    }

    interface Cache {
        String PREFIX = "daily_sign_in:";
        String USER = PREFIX + "user:";
        String SIGN_INFO = USER + "sign_info:";
        String TOTAL_DAYS = "totalDays";
        String CONSECUTIVE_DAYS = "consecutiveDays";
    }
}
