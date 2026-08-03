package com.summit.stp.comment.application.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.summit.stp.comment.domain.model.Comment;
import com.summit.stp.common.application.vo.UserSimpleVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentVO {
    @ApiModelProperty("评论ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;
    @ApiModelProperty("根评论ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long rootId;
    @ApiModelProperty("发布者id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long publisherId;
    @ApiModelProperty("父评论ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long parentId;
    @ApiModelProperty("帖子ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long postId;
    @ApiModelProperty("帖子发布者id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
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

    public static CommentVO fromModel(Comment comment) {
        if (comment == null) {
            return null;
        }
        return CommentVO.builder()
                .id(comment.getId())
                .rootId(comment.getRootId())
                .parentId(comment.getParentId())
                .postId(comment.getPostId())
                .postStatus(comment.getPostStatus() != null && comment.getPostStatus() == 1)
                .isAudit(comment.getIsAudit())
                .type(comment.getType() != null ? comment.getType().getCode() : null)
                .content(comment.getContent())
                .createTime(comment.getCreateTime())
                .isTop(comment.getIsTop())
                .status(comment.getStatus())
                .ipLocation(comment.getIpLocation())
                .clientType(comment.getClientType())
                .publisherId(comment.getPublisherId())
                .item(comment.getItem())
                .extra(comment.getExtra())
                .build();
    }
}