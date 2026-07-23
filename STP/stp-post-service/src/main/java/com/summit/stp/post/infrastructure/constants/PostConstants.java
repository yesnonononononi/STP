package com.summit.stp.post.infrastructure.constants;

public interface PostConstants {
    interface Business {
        int MAX_TITLE_LENGTH = 100;
        int MAX_CONTENT_LENGTH = 5000;
        int MAX_IMAGE_NUM = 6;
        String ORDER_TYPE_HOT = "hot";
        String ORDER_TYPE_NEWEST = "newest";
        String TEMPLATE_HTML_MSG = 
            "<div class=\"interaction-msg\" style=\"display: flex; align-items: center; justify-content: space-between; width: 100%%; gap: 12px;\">" +
            "    <div style=\"display: flex; align-items: center; gap: 12px;\">" +
            "        <img src=\"%s\" alt=\"avatar\" style=\"width: 40px; height: 40px; border-radius: 50%%; object-fit: cover; border: 1.5px solid #3b82f6;\" />" +
            "        <div>" +
            "            <div style=\"font-weight: 600; color: #1f2937; font-size: 14px;\">%s</div>" +
            "            <div style=\"font-size: 13px; color: #4b5563; margin-top: 2px;\">%s了你的帖子</div>" +
            "            <div style=\"font-size: 11px; color: #9ca3af; margin-top: 4px;\">%s</div>" +
            "        </div>" +
            "    </div>" +
            "    <div style=\"max-width: 180px; padding: 8px 12px; background: #f3f4f6; border-radius: 6px; font-size: 12px; color: #374151; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; border-left: 3px solid #3b82f6; font-weight: 500;\">" +
            "        %s" +
            "    </div>" +
            "</div>";
        int MAX_POST_SCORE_UPDATE_ONCE = 1000;
        long HOT_UPDATE_FALLBACK_DAYS = 7;
        int TAG_POSTS_REBUILD_LIMIT = 5000;
        long TAG_GLOBAL_HOT_CACHE_TTL_SEC = 3;
        long TAG_GLOBAL_HOT_CACHE_MAX_SIZE = 10;
        long TAG_POSTS_CACHE_TTL_SEC = 172800L;
        int TAG_POSTS_CACHE_TTL_RANDOM_SEC = 1800;
        int TAG_GLOBAL_HOT_POSTS_LIMIT = 5000;
        int DEFAULT_PAGE_SIZE = 10;
        double CURSOR_SCORE_OFFSET = 1.0;
        
        Integer CREATOR_MAX_DISPLAY_SIZE = 20;
        double MAX_IMAGE_SIZE_MB = 10.0;
        double MAX_VIDEO_SIZE_MB = 50.0;
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
    }
}
