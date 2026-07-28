package com.summit.stp.user.api.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "UpdateUserFollowRequest", description = "更新关注关系请求参数")
public class UpdateUserFollowRequest {
    @ApiModelProperty(value = "主键ID", required = true, example = "1")
    private Long id;

    @ApiModelProperty(value = "状态：1-正常关注，2-已取消，3-互相关注", required = true, example = "1")
    private Integer status;

    @ApiModelProperty(value = "关注来源", example = "search")
    private String source;
}
