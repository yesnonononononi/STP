package com.summit.stp.comment.domain.util;

import java.sql.Timestamp;

public class ScoreUtil {

    /**
     * 计算评论,帖子热度
     * @param recentLikeCount 最近1小时点赞数
     * @param recentReplyCount 最近1小时回复数
     * @param createTime 帖子/评论创建时间
     * @return 热度
     */
    public static double calculateHotScore(long recentLikeCount, long recentReplyCount, Timestamp createTime) {
        long diffMillis = System.currentTimeMillis() - createTime.getTime();
        double hours = diffMillis / (1000.0 * 3600);  // 转换为小时
        double score = (recentLikeCount + recentReplyCount * 3.0) / Math.pow(hours + 2, 1.5);
        return Math.ceil(score);
    }
}
