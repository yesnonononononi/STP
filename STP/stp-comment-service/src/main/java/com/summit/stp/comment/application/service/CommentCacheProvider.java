package com.summit.stp.comment.application.service;

import com.summit.stp.comment.application.vo.CommentVO;


import java.util.List;
import java.util.Map;

public interface CommentCacheProvider {
    String CREATOR_IS_LIKE = "CREATOR_IS_LIKE";
    String CREATOR_IS_REPLIED = "CREATOR_IS_REPLIED";
    String IS_LIKE = "IS_LIKE";
    String LIKE_COUNT = "LIKE_COUNT";
    String REPLY_COUNT = "REPLY_COUNT";

    Map<Long, Map<String, Long>> getLikeCountAndReplyCount(List<Long> list);

    Map<Long, Map<String, Boolean>> getIsLikeAndReplyByIds(List<CommentVO> list);

    void init(Long commentId,Long replyCount,Long likeCount);
    boolean like(Long commentId);
    void reply(Long commentId);
    void markCacheChanged(Long commentId);
    void markReplyChanged(Long commentId);

    void loadCache(Long commentId);
    void loadCache(List<Long> commentIds);
    java.util.Set<Long> getLikeUserIds(Long commentId);
}
