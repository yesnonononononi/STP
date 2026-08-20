package com.summit.stp.admin.application.vo;

import com.summit.stp.post.domain.model.Post;
import com.summit.stp.post.domain.model.PostImage;
import com.summit.stp.post.domain.model.PostStatus;
import com.summit.stp.post.domain.model.PostType;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Builder
@Data
public class AdminPostVO {
    private final Long id;

    private final Long creatorId;

    private String title;

    private Integer type;

    private String content;

    private String mediaUrls;

    private List<String> urls;

    private Long replyCount;

    private Integer status;

    private final Timestamp createTime;

    private Timestamp updateTime;

    private Integer isTop;

    private Long viewCount;

    private Long likeCount;

    private Long collectCount;

    private Double hotScore;

    private Integer visibleScope;
}
