package com.summit.stp.user.application.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "UserFollowVO", description = "用户关注详情展示数据")
public class UserFollowVO {
    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("关注者用户ID")
    private Long followerId;

    @ApiModelProperty("被关注者用户ID")
    private Long followeeId;

    @ApiModelProperty("状态：1-正常关注，2-已取消，3-互相关注")
    private Integer status;

    @ApiModelProperty("关注来源")
    private String source;

    @ApiModelProperty("关注创建时间")
    private Timestamp createTime;

    @ApiModelProperty("最后更新时间")
    private Timestamp updateTime;
}
