package com.summit.stp.tag.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Data
@TableName("posts")
@Builder
@ApiModel(value = "PostsPO", description = "帖子数据持久化实体")
public class PostsPO {
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "帖子ID")
    private Long id;
    @TableField(fill = FieldFill.INSERT)
    private Long publicId;

    @ApiModelProperty(value = "发布者用户ID")
    private long creatorId;

    @ApiModelProperty(value = "帖子标题")
    private String title;

    @ApiModelProperty(value = "帖子类型: image/video/audio/text")
    private int type;

    @ApiModelProperty(value = "文本内容")
    private String content;

    @ApiModelProperty(value = "媒体资源URL列表")
    private String mediaUrls;

    @ApiModelProperty(value = "回复数")
    private Integer replyCount;

    @ApiModelProperty(value = "状态: 0已删除, 1公开已发布, 2草稿/私密")
    private Integer status;

    @ApiModelProperty(value = "创建时间")
    private Timestamp createTime;

    @ApiModelProperty(value = "更新时间")
    private Timestamp updateTime;

    @ApiModelProperty(value = "是否置顶: 0否, 1是")
    private Integer isTop;

    @ApiModelProperty(value = "浏览数")
    private Long viewCount;

    @ApiModelProperty(value = "点赞数")
    private Long likeCount;

    @ApiModelProperty(value = "收藏数")
    private Long collectCount;

    @ApiModelProperty(value = "热度")
    private Long hotScore;

    @ApiModelProperty(value = "可见范围 1-全局可见 2-仅自己可见 3-仅好友可见 ")
    private Integer visibleScope;
}
