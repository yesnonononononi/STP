package com.summit.stp.comment.comment.application.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
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
@ApiModel(value = "CommentImageVO", description = "评论图片展示数据")
public class CommentImageVO {
    @ApiModelProperty("自增主键ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @ApiModelProperty("所属评论ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long commentId;

    @ApiModelProperty("图片名称")
    private String imageName;

    @ApiModelProperty("图片类型")
    private Integer typeCode;

    @ApiModelProperty("图片URL")
    private String imageUrl;

    @ApiModelProperty("宽度")
    private Integer width;

    @ApiModelProperty("高度")
    private Integer height;

    @ApiModelProperty("文件大小 (字节)")
    private Integer size;

    @ApiModelProperty("排序序号")
    private Integer sortOrder;

    @ApiModelProperty("状态：1-正常，2-违规")
    private Integer status;

    @ApiModelProperty("创建时间")
    private Timestamp createTime;
}
