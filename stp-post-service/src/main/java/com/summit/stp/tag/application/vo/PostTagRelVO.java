package com.summit.stp.tag.application.vo;

import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Builder
@Data
public class PostTagRelVO {
    private final Long id;
    private final Long postId;
    private final String tagId;
    private final Timestamp createTime;
}
