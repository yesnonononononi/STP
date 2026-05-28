package com.summit.stp.post.application.vo;

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
@ApiModel(value = "TagVO", description = "标签详细展示信息")
public class TagVO {
    @ApiModelProperty("标签ID")
    private Long id;

    @ApiModelProperty("标签名称")
    private String tagName;

    @ApiModelProperty("权重/排序")
    private Integer sort;

    @ApiModelProperty("使用次数")
    private Integer useCount;

    @ApiModelProperty("状态: 0禁用, 1启用")
    private Integer status;

    @ApiModelProperty("创建时间")
    private Timestamp createTime;
}
