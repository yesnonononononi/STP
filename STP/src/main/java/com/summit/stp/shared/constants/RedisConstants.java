package com.summit.stp.shared.constants;

import org.jspecify.annotations.NonNull;

import java.time.Duration;

public interface RedisConstants {

    interface  Coupon{
        String PREFIX = "coupon:";
        String USE_LOCK = PREFIX + "use:lock:";
        String REFUND_LOCK = PREFIX+"fund:lock:";
        String ACTIVITY = PREFIX + "activity:";
        String STOCK =ACTIVITY+"stock:";
        String USER_LIMITED_HASH = ACTIVITY+"user:limited:";
        String SCHEDULED_REFRESH_COUPON_STOCK = PREFIX + "scheduler";
    }
    /**
     * 用户认证相关缓存键
     */
    interface Auth {
        String PREFIX = "user:auth:";
        String ACCESS_TOKEN = PREFIX + "token:access:";
        String REFRESH_TOKEN = PREFIX + "token:refresh:";
        String ACCESS_SESSION = PREFIX + "session:access:";
        String REFRESH_SESSION = PREFIX + "session:refresh:";
    }

    /**
     * 订单相关缓存与锁
     */
    interface Order {
        String PREFIX = "order:";
        String TIMEOUT_ZSET = PREFIX + "timeout:zset";
        String TIMEOUT_LOCK = PREFIX + "timeout:lock";
        String PAY_SUCCESS_MARK = PREFIX + "pay:success:";
        String LOCK_PAY = PREFIX + "pay:lock:";
    }

    /**
     * 会员相关缓存与锁
     */
    interface Member {
        String PREFIX = "member:";
        String LEVEL_CONFIG = PREFIX + "level:config";
        String PAY_CONSUME_MARK = PREFIX + "pay:consume:";
        String LOCK_PAY = PREFIX + "pay:lock:";
    }

    /**
     * 帖子相关缓存键与锁
     */
    interface Post {
        String PREFIX = "post:";
        String LIKE = PREFIX + "like:";
        String COLLECT = PREFIX + "collect:";
        String VIEW = PREFIX + "view:";
        String LIKE_COLLECT_CHANGED = PREFIX + "changed:like_collect:set";
        String LIKE_COLLECT_CHANGED_RUN_PREFIX = PREFIX + "changed:like_collect:backup:";
        String LIKE_COLLECT_CHANGED_LOCK = PREFIX + "changed:like_collect:lock";
        String VIEW_CHANGED = PREFIX + "changed:view:set";
        String VIEW_CHANGED_RUN_PREFIX = PREFIX + "changed:view:backup:";
        String VIEW_CHANGED_LOCK = PREFIX + "changed:view:lock";
        String REPLY_CHANGED = PREFIX + "changed:reply:set";
        String REPLY_CHANGED_RUN_PREFIX = PREFIX + "changed:reply:backup:";
        String REPLY_CHANGED_LOCK = PREFIX + "changed:reply:lock";
        String CHANGED = LIKE_COLLECT_CHANGED;
        String CHANGED_RUN_PREFIX = LIKE_COLLECT_CHANGED_RUN_PREFIX;
        String CHANGED_LOCK = LIKE_COLLECT_CHANGED_LOCK;
    }

    interface Comment{
        String PREFIX = "comment:";
        String LIKE = PREFIX + "like:";
        String REPLY = PREFIX + "reply:";
        String LIKE_COUNT = LIKE + "count:";
        String REPLY_COUNT = REPLY + "count:";
        String CHANGED = PREFIX + "changed:set";
        String CHANGED_RUN_PREFIX = PREFIX + "changed:backup:";
        String CHANGED_LOCK = PREFIX + "changed:lock";
        String REPLY_CHANGED = PREFIX + "changed:reply:set";
        String REPLY_CHANGED_RUN_PREFIX = PREFIX + "changed:reply:backup:";
        String REPLY_CHANGED_LOCK = PREFIX + "changed:reply:lock";
        @NonNull Long EXPIRE_TIME = 7L;
        String HOT_SCORE_LOCK = PREFIX+"hot_score_lock";
    }

    interface Rank{
        String PREFIX = "rank:";
        String LOCK = PREFIX + "lock:";
        String POST_HOT_ZSET = PREFIX + "post_hot:zset";
        String TOPIC_USE_ZSET = PREFIX + "topic_use:zset";
    }
}
