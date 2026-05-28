package com.summit.stp.post.infrastructure.persistence.po;

import lombok.Builder;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
