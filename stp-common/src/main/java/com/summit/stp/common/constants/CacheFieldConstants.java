package com.summit.stp.common.constants;

/**
 * Redis Hash 字段名常量，消除帖子缓存中的硬编码字符串
 */
public final class CacheFieldConstants {

    private CacheFieldConstants() {}

    public static final String CREATOR_ID = "creatorId";
    public static final String TITLE = "title";
    public static final String TYPE = "type";
    public static final String CONTENT = "content";
    public static final String STATUS = "status";
    public static final String CREATE_TIME = "createTime";
    public static final String UPDATE_TIME = "updateTime";
    public static final String IS_TOP = "isTop";
    public static final String VISIBLE_SCOPE = "visibleScope";
    public static final String LIKE_COUNT = "likeCount";
    public static final String COLLECT_COUNT = "collectCount";
    public static final String REPLY_COUNT = "replyCount";
    public static final String VIEW_COUNT = "viewCount";
    public static final String EXTRA_MEDIA_URL = "extraMediaUrl";
    public static final String TAG_IDS = "tagIds";

    /** 互动状态 map 的 key（非 Hash 字段，用于 getIsCollectedOrLiked 返回值） */
    public static final String INTERACTION_LIKE = "LIKE";
    public static final String INTERACTION_COLLECT = "COLLECT";
}
