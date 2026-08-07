package com.summit.stp.post.application.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "PostImageVO", description = "帖子图片展示数据")
public class PostImageVO {
    @ApiModelProperty("图片关联自增主键ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @ApiModelProperty("所属帖子ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long postId;

    @ApiModelProperty("图片URL")
    private String imageUrl;

    @ApiModelProperty("图片宽度")
    private Integer width;

    @ApiModelProperty("图片高度")
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
