package com.summit.stp.coupon.infrastructure.constants;

public interface CouponConstants {
    interface Business {}

    interface Cache {
        String PREFIX = "coupon:";
        String USE_LOCK = PREFIX + "use:lock:";
        String REFUND_LOCK = PREFIX + "fund:lock:";
        String ACTIVITY = PREFIX + "activity:";
        String STOCK = ACTIVITY + "stock:";
        String USER_LIMITED_HASH = ACTIVITY + "user:limited:";
        String SCHEDULED_REFRESH_COUPON_STOCK = PREFIX + "scheduler";
        String PREWARM_LOCK = PREFIX + "prewarm:";
    }
}
