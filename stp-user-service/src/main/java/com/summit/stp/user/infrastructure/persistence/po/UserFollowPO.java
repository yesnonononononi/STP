package com.summit.stp.user.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;
@Builder
@Data
@TableName("user_follow")
@ApiModel(value = "UserFollowPO", description = "用户关注关系数据持久化实体")
public class UserFollowPO {
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键ID")
    private Long id;

    @ApiModelProperty(value = "关注者用户ID")
    private Long followerId;

    @ApiModelProperty(value = "被关注者用户ID")
    private Long followeeId;

    @ApiModelProperty(value = "状态：1-正常关注，2-已取消，3-互相关注")
    private Integer status;

    @ApiModelProperty(value = "关注来源")
    private String source;

    @ApiModelProperty(value = "关注创建时间")
    private Timestamp createTime;

    @ApiModelProperty(value = "最后更新时间")
    private Timestamp updateTime;
}
