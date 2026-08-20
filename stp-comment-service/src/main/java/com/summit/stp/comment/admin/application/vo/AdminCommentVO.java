package com.summit.stp.comment.admin.application.vo;

import com.summit.stp.comment.comment.domain.model.Comment;
import com.summit.stp.comment.comment.domain.model.CommentType;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Builder
public class AdminCommentVO {
    private Long id;

    private Long rootId;

    private Long publisherId;

    private Long parentId;

    private Long postId;

    private Integer postStatus;

    private CommentType type;

    private String content;

    private Timestamp createTime;

    private Long replyCount;

    private Integer isTop;

    private Integer status;

    private String reportReason;

    private Comment.Item item;

    private Comment.Extra extra;

    private String ipLocation;

    private String clientType;

    private Long likeCount;

    private Timestamp updateTime;
}
