package com.summit.stp.post.application.command;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CreatePostImageCommand {
    private final Long postId;
    private final List<String> imageUrl;
    private final Integer width;
    private final Integer height;
    private final Integer size;
    private final Integer sortOrder;
    private final Integer status;
}
