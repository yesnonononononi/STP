package com.summit.stp.post.api.dto.request;

import lombok.Data;



@Data
public class QueryPostListPageRequest {
    private String cursor;
    private Boolean self;
    private Long creatorId;
    private Integer status;
}
