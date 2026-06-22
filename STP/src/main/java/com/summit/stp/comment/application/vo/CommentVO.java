package com.summit.stp.comment.application.vo;

import com.summit.stp.comment.domain.model.Comment;
import com.summit.stp.comment.domain.model.CommentType;
import com.summit.stp.user.application.vo.UserSimpleVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentVO {
    @ApiModelProperty("评论ID")
    private Long id;
    @ApiModelProperty("根评论ID")
    private Long rootId;
    @ApiModelProperty("发布者id")
    private Long publisherId;
    @ApiModelProperty("父评论ID")
    private Long parentId;
    @ApiModelProperty("帖子ID")
    private Long postId;
    @ApiModelProperty("帖子发布者id")
    private Long postPublisherId;
    @ApiModelProperty("帖子状态")
    private Boolean postStatus;
    @ApiModelProperty("是否审核")
    private Boolean isAudit;
    @ApiModelProperty("评论类型")
    private Integer type;
    @ApiModelProperty("评论内容")
    private String content;
    @ApiModelProperty("创建时间")
    private Timestamp createTime;
    @ApiModelProperty("发布者")
    private UserSimpleVO publisher;
    @ApiModelProperty("是否置顶")
    private Integer isTop;
    @ApiModelProperty("评论状态")
    private Integer status;
    @ApiModelProperty("点赞,回复信息")
    private Comment.Item item;
    @ApiModelProperty("额外信息")
    private Comment.Extra extra;
    @ApiModelProperty("ip地址")
    private String ipLocation;
    @ApiModelProperty("客户端类型")
    private String clientType;
    @ApiModelProperty("评论热度")
    private Double hotScore;


}