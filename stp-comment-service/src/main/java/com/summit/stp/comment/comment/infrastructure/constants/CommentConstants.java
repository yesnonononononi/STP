package com.summit.stp.comment.comment.infrastructure.constants;

public interface CommentConstants {
    interface Business {
        int MAX_CONTENT_LENGTH = 500;
        int MAX_IMAGE_NUM = 6;
    }

    interface Cache {
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
        Long EXPIRE_TIME = 7L;
        String HOT_SCORE_LOCK = PREFIX + "hot_score_lock";
    }
}
