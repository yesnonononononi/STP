package com.summit.stp.post.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
}
