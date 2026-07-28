package com.summit.stp.user.api.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "更新用户偏好配置请求")
public class UpdateUserSettingRequest {
    @ApiModelProperty(value = "是否显示已删除帖子: 0否, 1是")
    private Integer showDelPost;

    @ApiModelProperty(value = "是否开启个性化推荐: 0否, 1是")
    private Integer customizationRecommend;
}
