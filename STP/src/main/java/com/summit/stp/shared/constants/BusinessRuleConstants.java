package com.summit.stp.shared.constants;

import java.util.prefs.Preferences;

/**
 * 业务校验规则及限制常量中心
 */
public final class BusinessRuleConstants {
    public static final class Emoji{

        public static final int MAX_NAME_LENGTH = 10;
    }

    private BusinessRuleConstants() {}

    public static final class User {
        /**
         * 简介最高字数限制
         */
        public static final int MAX_INTRODUCE_LENGTH = 100;

        /**
         * 昵称最高字数限制
         */
        public static final int MAX_NICK_LENGTH = 20;
    }

    public static final class Post {
        /**
         * 帖子标题最高字数限制
         */
        public static final int MAX_TITLE_LENGTH = 100;

        /**
         * 帖子内容最高字数限制
         */
        public static final int MAX_CONTENT_LENGTH = 5000;

        /**
         * 帖子最多容纳图片张数限制
         */
        public static final int MAX_IMAGE_NUM = 6;
    }
    public static final class Message{

        public static final int MAX_CONTENT_LENGTH = 1000;
    }

    public static final class Comment {
        /**
         * 评论内容最高字数限制
         */
        public static final int MAX_CONTENT_LENGTH = 500;

        /**
         * 评论最多容纳图片张数限制
         */
        public static final int MAX_IMAGE_NUM = 6;
    }

    public static final class File {
        /**
         * 上传图片最大容量 (MB)
         */
        public static final double MAX_IMAGE_SIZE_MB = 10.0;

        /**
         * 上传视频最大容量 (MB)
         */
        public static final double MAX_VIDEO_SIZE_MB = 50.0;
    }
}
