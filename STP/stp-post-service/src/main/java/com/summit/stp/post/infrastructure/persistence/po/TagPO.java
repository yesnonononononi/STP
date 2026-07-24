package com.summit.stp.post.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.sql.Timestamp;

@Data
@TableName("tag")
@ApiModel(value = "TagPO", description = "标签数据持久化实体")
public class TagPO {
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "标签ID")
    private Long id;
    @TableField(fill = FieldFill.INSERT)
    private Long publicId;

    @ApiModelProperty(value = "标签UUID")
    private String uuid;

    @ApiModelProperty(value = "标签名")
    private String tagName;


    @ApiModelProperty(value = "权重/排序")
    private Integer sort;

    @ApiModelProperty(value = "使用次数")
    private Integer useCount;

    @ApiModelProperty(value = "状态: 0禁用, 1启用")
    private Integer status;

    @ApiModelProperty(value = "创建时间")
    private Timestamp createTime;
}
