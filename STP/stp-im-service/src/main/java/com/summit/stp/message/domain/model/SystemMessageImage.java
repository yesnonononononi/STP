package com.summit.stp.message.domain.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SystemMessageImage {
    private final Long id;
    private final Long messageId;
    private String image;
    private String status;
    private final String createTime;
    private String updateTime;
}
