package com.summit.stp.tag.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Builder
@Data
@TableName("post_tag_rel")
@ApiModel(value = "PostTagRelPO", description = "帖子标签关联数据持久化实体")
public class PostTagRelPO {
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "自增主键ID")
    private Long id;

    @ApiModelProperty(value = "帖子ID")
    private long postId;

    @ApiModelProperty(value = "标签ID")
    private long tagId;

    @ApiModelProperty(value = "创建/关联时间")
    private Timestamp createTime;
}
