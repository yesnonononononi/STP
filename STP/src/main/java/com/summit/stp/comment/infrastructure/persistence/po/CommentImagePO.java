package com.summit.stp.comment.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.sql.Timestamp;

@Data
@TableName("comment_image")
@ApiModel(value = "CommentImagePO", description = "评论图片数据持久化实体")
public class CommentImagePO {
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "自增主键")
    private Long id;

    @ApiModelProperty(value = "所属评论ID")
    private Long commentId;

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

    @ApiModelProperty(value = "状态")
    private Integer status;

    @ApiModelProperty(value = "创建时间")
    private Timestamp createTime;
}
