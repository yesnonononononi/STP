package com.summit.stp.comment.comment.application.command;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateCommentImageCommand {
    private final Long commentId;
    private final String imageUrl;
    private final Integer width;
    private final Integer height;
    private final Integer size;
    private final Integer sortOrder;
}
