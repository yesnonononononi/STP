package com.summit.stp.post.api.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.List;

@Data
@ApiModel(value = "CreatePostImageRequest", description = "创建帖子图片请求参数")
public class CreatePostImageRequest {
    @ApiModelProperty(value = "所属帖子ID", required = true, example = "20001")
    private Long postId;

    @ApiModelProperty(value = "图片URL列表", required = true)
    private List<String> imageUrl;

    @ApiModelProperty(value = "图片宽度", example = "800")
    private Integer width;

    @ApiModelProperty(value = "图片高度", example = "600")
    private Integer height;

    @ApiModelProperty(value = "文件大小 (字节)", example = "102400")
    private Integer size;

    @ApiModelProperty(value = "排序序号 (默认0)", example = "0")
    private Integer sortOrder;
}
