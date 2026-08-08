package com.summit.stp.tag.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.sql.Timestamp;

@Builder
@EqualsAndHashCode
@Getter
public class PostTag {
    private final Long id;
    private final   Long postId;
    private final Long tagId;
    private final Timestamp createTime;
}
