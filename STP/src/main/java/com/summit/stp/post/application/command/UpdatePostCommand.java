package com.summit.stp.post.application.command;

import lombok.Builder;
import lombok.Data;
import com.summit.stp.post.api.dto.request.ImageInfo;
import java.util.List;

@Data
@Builder
public class UpdatePostCommand {
    private Long id;
    private long creatorId;
    private String title;
    private Integer type;
    private String content;
    private List<ImageInfo> mediaUrls;
    private Integer status;
    private List<Long> tagIds;
}
