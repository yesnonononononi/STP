package com.summit.stp.comment.comment.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@TableName("comments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "CommentsPO", description = "评论数据持久化实体")
public class CommentsPO {
    @TableId(type = IdType.AUTO)
    @ApiModelProperty("评论ID")
    private Long id;

    @ApiModelProperty("所属帖子ID")
    private Long postId;

    @ApiModelProperty("评论人用户ID")
    private Long userId;

    @ApiModelProperty("父级评论ID (回复别人的评论时使用，一级评论为0或null)")
    private Long parentId;

    @ApiModelProperty("根评论ID (用于快速获取整个评论树，一级评论为其自身ID或null)")
    private Long rootId;

    @ApiModelProperty("评论内容")
    private String content;

    @ApiModelProperty("评论创建时间")
    private Timestamp createTime;

    @ApiModelProperty("评论类型: 1文字, 2图片, 3视频, 4音频")
    private Integer type;

    @ApiModelProperty("是否置顶: 0否, 1是")
    private Integer isTop;

    @ApiModelProperty("评论状态: 0已删除, 1正常")
    private Integer status;

    @ApiModelProperty("举报原因")
    private String reportReason;

    @ApiModelProperty("额外扩展信息 (JSON 格式，包含媒体类型、图片元数据、媒体URL等)")
    private String extra;

    @ApiModelProperty("回复数 / 子评论数")
    private Long replyCount;

    @ApiModelProperty("修改时间")
    private Timestamp updateTime;

    @ApiModelProperty("IP归属地")
    private String ipLocation;

    @ApiModelProperty("客户端类型: ios, android, web等")
    private String clientType;

    @ApiModelProperty("评论热度")
    private Long hotScore;

    @ApiModelProperty("点赞数")
    private Long likeCount;
}
