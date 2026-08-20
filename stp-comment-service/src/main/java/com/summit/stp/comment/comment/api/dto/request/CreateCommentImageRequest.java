package com.summit.stp.comment.comment.api.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "CreateCommentImageRequest", description = "创建评论图片请求参数")
public class CreateCommentImageRequest {
    @ApiModelProperty(value = "所属评论ID", required = true, example = "30001")
    private Long commentId;

    @ApiModelProperty(value = "图片URL", required = true, example = "https://example.com/comment_image.jpg")
    private String imageUrl;

    @ApiModelProperty(value = "图片宽度", example = "800")
    private Integer width;

    @ApiModelProperty(value = "图片高度", example = "600")
    private Integer height;

    @ApiModelProperty(value = "文件大小 (字节)", example = "102400")
    private Integer size;

    @ApiModelProperty(value = "排序序号 (默认0)", example = "0")
    private Integer sortOrder;
}
