package com.summit.stp.post.api.dto.request;

import lombok.Data;

@Data
public class QueryPostListPageRequest {
    private Long cursor;
    private Boolean self;
    private Long creatorId;
    private Integer status;
    private String orderType;
}
