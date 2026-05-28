package com.summit.stp.comment.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.sql.Timestamp;

@TableName("comments")
@Data
@ApiModel(value = "CommentsPO", description = "评论数据持久化实体")
public class CommentsPO {
    @TableId(type = IdType.AUTO)
    @ApiModelProperty("评论ID")
    private Long id;

    @ApiModelProperty("所属帖子ID")
    private long postId;

    @ApiModelProperty("评论人用户ID")
    private long userId;

    @ApiModelProperty("父级评论ID (回复别人的评论时使用，一级评论为0或null)")
    private Long parentId;

    @ApiModelProperty("根评论ID (用于快速获取整个评论树，一级评论为其自身ID或null)")
    private Long rootId;

    @ApiModelProperty("评论内容")
    private String content;

    @ApiModelProperty("评论创建时间")
    private Timestamp createTime;
}
