package com.summit.stp.post.application.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.summit.stp.user.api.vo.UserSimpleVO;
import com.summit.stp.tag.application.vo.TagVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.sql.Timestamp;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "PostVO", description = "帖子公开展示详细信息")
public class PostVO {
    @ApiModelProperty("帖子ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @ApiModelProperty("发布者用户ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonSerialize(using = ToStringSerializer.class)
    private long creatorId;

    @ApiModelProperty("帖子标题")
    private String title;

    @ApiModelProperty("帖子类型: 1文字, 2图片, 3视频, 4音频")
    private Integer type;

    @ApiModelProperty("文本内容")
    private String content;

    @ApiModelProperty("其他媒体资源url,如语音,视频")
    private String extraMediaUrl;

    @ApiModelProperty("媒体资源URL列表")
    private List<PostImageVO> mediaUrls;

    @ApiModelProperty("获赞数")
    private Long likeCount;

    @ApiModelProperty("回复数")
    private Long replyCount;

    @ApiModelProperty("收藏数")
    private Long collectCount;

    @ApiModelProperty("是否点赞")
    private Boolean isLike;

    @ApiModelProperty("是否收藏")
    private Boolean isCollect;

    @ApiModelProperty("状态: 0已删除, 1公开已发布, 2草稿/私密")
    private Integer status;

    @ApiModelProperty("创建时间")
    private Timestamp createTime;

    @ApiModelProperty("更新时间")
    private Timestamp updateTime;

    @ApiModelProperty("发布者展示信息")
    private UserSimpleVO publisher;

    @ApiModelProperty("帖子关联的标签详情列表")
    private List<TagVO> tags;

    @ApiModelProperty("是否置顶: 0否, 1是")
    private Integer isTop;

    @ApiModelProperty("浏览量")
    private Long viewCount;

    private Double hotScore;

    private Integer visibleScope;
}

