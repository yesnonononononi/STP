package com.summit.stp.post.api.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "ImageInfo", description = "帖子图片详细信息")
public class ImageInfo {
    @ApiModelProperty("图片URL")
    private String url;

    @ApiModelProperty("图片宽度")
    private Integer width;

    @ApiModelProperty("图片高度")
    private Integer height;
}
