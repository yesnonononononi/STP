package com.summit.stp.comment.comment.api.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "UpdateCommentImageRequest", description = "更新评论图片请求参数")
public class UpdateCommentImageRequest {
    @ApiModelProperty(value = "主键ID", required = true, example = "1")
    private Long id;

    @ApiModelProperty(value = "图片URL", example = "https://example.com/comment_image_new.jpg")
    private String imageUrl;

    @ApiModelProperty(value = "图片宽度", example = "800")
    private Integer width;

    @ApiModelProperty(value = "图片高度", example = "600")
    private Integer height;

    @ApiModelProperty(value = "文件大小 (字节)", example = "102400")
    private Integer size;

    @ApiModelProperty(value = "排序序号", example = "1")
    private Integer sortOrder;

    @ApiModelProperty(value = "状态：1-正常，2-违规", example = "1")
    private Integer status;
}
