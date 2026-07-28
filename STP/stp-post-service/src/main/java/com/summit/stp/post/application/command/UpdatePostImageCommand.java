package com.summit.stp.post.application.command;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdatePostImageCommand {
    private final Long id;
    private final String imageUrl;
    private final Integer width;
    private final Integer height;
    private final Integer size;
    private final Integer sortOrder;
    private final Integer status;
}
