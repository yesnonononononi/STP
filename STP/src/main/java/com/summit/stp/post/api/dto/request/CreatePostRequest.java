package com.summit.stp.post.api.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.List;

@Data
@ApiModel(value = "CreatePostRequest", description = "创建发布新帖子请求参数")
public class CreatePostRequest {
    @ApiModelProperty(value = "帖子标题", required = true)
    private String title;

    @ApiModelProperty(value = "帖子类型: 1文字, 2图片, 3视频, 4音频", required = true)
    private Integer type;

    @ApiModelProperty(value = "文本内容")
    private String content;

    @ApiModelProperty(value = "媒体资源URL及属性列表")
    private List<ImageInfo> mediaUrls;

    @ApiModelProperty(value = "状态: 1公开已发布, 2草稿/私密", required = true)
    private Integer status;

    @ApiModelProperty(value = "帖子关联的标签ID列表")
    private List<Long> tagIds;
}
