package com.summit.stp.post.api.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "UpdateTagRequest", description = "更新标签请求参数")
public class UpdateTagRequest {
    @ApiModelProperty(value = "标签ID", required = true)
    private Long id;

    @ApiModelProperty(value = "标签名称")
    private String tagName;

    @ApiModelProperty(value = "权重/排序")
    private Integer sort;

    @ApiModelProperty(value = "状态: 0禁用, 1启用")
    private Integer status;
}
