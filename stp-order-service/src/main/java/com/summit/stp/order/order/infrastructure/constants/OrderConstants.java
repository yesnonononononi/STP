package com.summit.stp.order.order.infrastructure.constants;

public interface OrderConstants {
    interface Business {
        long TIMEOUT_MILLIS = 600_000L;
        long TIMEOUT_SCAN_INTERVAL_MILLIS = 5_000L;
        int MYSQL_TIMEOUT_FALLBACK_BATCH_SIZE = 100;
    }

    interface Cache {
        String PREFIX = "order:";
        String TIMEOUT_ZSET = PREFIX + "timeout:zset";
        String TIMEOUT_LOCK = PREFIX + "timeout:lock";
        String PAY_SUCCESS_MARK = PREFIX + "pay:success:";
        String LOCK_PAY = PREFIX + "pay:lock:";
    }
}
