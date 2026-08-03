package com.summit.stp.message.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@TableName("system_message_image")
public class SystemMessageImagePO {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField(fill = FieldFill.INSERT)
    private Long publicId;
    private Long messageId;
    private String image;
    private String status;
    private String createTime;
    private String updateTime;
}
