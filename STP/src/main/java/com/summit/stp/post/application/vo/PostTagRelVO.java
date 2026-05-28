package com.summit.stp.post.application.vo;

import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Builder
@Data
public class PostTagRelVO {
    private final Long id;
    private final Long postId;
    private final Long tagId;
    private final Timestamp createTime;
}
