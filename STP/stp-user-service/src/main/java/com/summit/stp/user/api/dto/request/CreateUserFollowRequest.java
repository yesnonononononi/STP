package com.summit.stp.user.api.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "CreateUserFollowRequest", description = "创建关注关系请求参数")
public class CreateUserFollowRequest {
    @ApiModelProperty(value = "关注者用户ID", required = true, example = "10001")
    private Long followerId;

    @ApiModelProperty(value = "被关注者用户ID", required = true, example = "10002")
    private Long followeeId;

    @ApiModelProperty(value = "关注来源", example = "search")
    private String source;
}
