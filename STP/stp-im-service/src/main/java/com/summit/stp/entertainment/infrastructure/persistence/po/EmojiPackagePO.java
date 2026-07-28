package com.summit.stp.entertainment.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Builder
@TableName("emoji_package")
public class EmojiPackagePO {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField(fill = FieldFill.INSERT)
    private Long publicId;
    private String name;
    private String description;
    private String coverImage;
    private Integer status;
    private Timestamp createTime;
    private Timestamp updateTime;
}
