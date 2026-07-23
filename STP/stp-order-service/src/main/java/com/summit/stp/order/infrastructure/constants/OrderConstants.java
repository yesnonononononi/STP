package com.summit.stp.order.infrastructure.constants;

public interface OrderConstants {
    interface Business {
        long TIMEOUT = 60 * 1000;
    }

    interface Cache {
        String PREFIX = "order:";
        String TIMEOUT_ZSET = PREFIX + "timeout:zset";
        String TIMEOUT_LOCK = PREFIX + "timeout:lock";
        String PAY_SUCCESS_MARK = PREFIX + "pay:success:";
        String LOCK_PAY = PREFIX + "pay:lock:";
    }
}
