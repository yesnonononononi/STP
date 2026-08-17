package com.summit.stp.post.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Data
@TableName("post_image")
@Builder
@ApiModel(value = "PostImagePO", description = "帖子图片数据持久化实体")
public class PostImagePO {
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "自增主键")
    private Long id;

    @ApiModelProperty(value = "所属帖子ID")
    private Long postId;

    @ApiModelProperty(value = "图片URL")
    private String imageUrl;

    @ApiModelProperty(value = "宽度")
    private Integer width;

    @ApiModelProperty(value = "高度")
    private Integer height;

    @ApiModelProperty(value = "文件大小 (字节)")
    private Integer size;

    @ApiModelProperty(value = "排序序号")
    private Integer sortOrder;

    @ApiModelProperty(value = "状态：1-正常，2-违规")
    private Integer status;

    @ApiModelProperty(value = "创建时间")
    private Timestamp createTime;
}
