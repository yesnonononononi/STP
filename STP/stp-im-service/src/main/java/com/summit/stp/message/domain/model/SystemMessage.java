package com.summit.stp.message.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Builder
@Getter
public class SystemMessage {
    private final Long id;
    private final Long fromUserId;
    private final List<String> images;
    private String content;
    private Integer status;
    private final Long associateUser;
    private final Integer type;
    private Instant publicTime;
    private final Instant createTime;
    private Instant updateTime;
}
