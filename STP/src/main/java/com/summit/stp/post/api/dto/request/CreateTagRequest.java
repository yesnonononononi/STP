package com.summit.stp.post.api.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "CreateTagRequest", description = "创建新标签请求参数")
public class CreateTagRequest {
    @ApiModelProperty(value = "标签名称", required = true)
    private String tagName;

    @ApiModelProperty(value = "权重/排序")
    private Integer sort;

    @ApiModelProperty(value = "状态: 0禁用, 1启用")
    private Integer status;
}
