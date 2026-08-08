package com.summit.stp.tag.api.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "CreateTagRequest", description = "创建新标签请求参数")
public class CreateTagRequest {
    @ApiModelProperty(value = "标签名称", required = true)
    private String tagName;

}
