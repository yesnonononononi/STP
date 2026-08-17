package com.summit.stp.post.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@TableName("post_collect")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "PostCollectPO", description = "帖子收藏关系实体")
public class PostCollectPO {
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键ID")
    private Long id;

    @ApiModelProperty(value = "帖子ID")
    private Long postId;

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "收藏时间")
    private Timestamp createTime;
}
