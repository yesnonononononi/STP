package com.summit.stp.post.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@TableName("post_like")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "PostLikePO", description = "帖子点赞关系实体")
public class PostLikePO {
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value = "主键ID")
    private Long id;
    @TableField(fill = FieldFill.INSERT)
    private Long publicId;

    @ApiModelProperty(value = "帖子ID")
    private Long postId;

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "点赞时间")
    private Timestamp createTime;
}
