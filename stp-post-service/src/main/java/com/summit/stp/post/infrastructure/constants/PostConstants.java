package com.summit.stp.post.infrastructure.constants;

public interface PostConstants {
    interface Business {
        int MAX_TITLE_LENGTH = 100;
        int MAX_CONTENT_LENGTH = 5000;
        int MAX_IMAGE_NUM = 6;
        String ORDER_TYPE_HOT = "hot";
        int MAX_POST_SCORE_UPDATE_ONCE = 1000;
        int TAG_POSTS_REBUILD_LIMIT = 5000;
        long TAG_GLOBAL_HOT_CACHE_TTL_SEC = 3;
        long TAG_GLOBAL_HOT_CACHE_MAX_SIZE = 10;
        long TAG_POSTS_CACHE_TTL_SEC = 172800L;
        int TAG_POSTS_CACHE_TTL_RANDOM_SEC = 1800;
        int TAG_GLOBAL_HOT_POSTS_LIMIT = 5000;
        int DEFAULT_PAGE_SIZE = 10;
        double CURSOR_SCORE_OFFSET = 1.0;
        
        Integer CREATOR_MAX_DISPLAY_SIZE = 20;
        double MAX_IMAGE_SIZE_MB = 20.0;
        double MAX_VIDEO_SIZE_MB = 50.0;
        
        double CREATOR_SCORE_POST = 10.0;
        double CREATOR_SCORE_LIKE = 2.0;
        double CREATOR_SCORE_FAN = 5.0;
    }

    interface Cache {
        String PREFIX = "post:";
        String DETAIL_PREFIX = PREFIX + "detail:";
        String TAG_DETAIL_PREFIX = "tag:detail:";
        String TAG_POSTS_PREFIX = PREFIX + "tag:posts:";
        String GLOBAL_HOT_POSTS_LOCAL_KEY = "global_hot_posts";
        String LIKE_SET_PREFIX = PREFIX + "like:";
        String COLLECT_SET_PREFIX = PREFIX + "collect:";
        String VIEW_LIMIT_PREFIX = PREFIX + "view:limit:";
        String CHANGED = PREFIX + "changed:set";
        String CHANGED_RUN_PREFIX = PREFIX + "changed:backup:";
        String CHANGED_LOCK = PREFIX + "changed:lock";
        String QUERY_NEWEST = PREFIX + "query:newest";
        String QUERY_HOT = PREFIX + "query:hot";
        String NEWEST_REBUILD_LOCK = PREFIX + "query:newest:rebuild:lock";
        String POST_HOT_LOCK = "rank:lock:post_hot";
        String ACTIVE_POST_KEYS = PREFIX + "active:keys";
        
        String RANK_PREFIX = "rank:";
        String RANK_LOCK = RANK_PREFIX + "lock:";
        String POST_HOT_ZSET = QUERY_HOT;
        String TOPIC_USE_ZSET = RANK_PREFIX + "topic_use:zset";
        String RANK_CREATOR_KEY =RANK_PREFIX+"hot:";
        String RANK_CREATOR_WEEKLY_PREFIX = RANK_PREFIX + "creator:weekly:";

        static String getCreatorWeeklyKey(java.time.LocalDate date) {
            java.time.temporal.WeekFields weekFields = java.time.temporal.WeekFields.of(java.util.Locale.getDefault());
            int year = date.get(weekFields.weekBasedYear());
            int week = date.get(weekFields.weekOfWeekBasedYear());
            return RANK_CREATOR_WEEKLY_PREFIX + year + "-" + String.format("%02d", week);
        }
    }
}
