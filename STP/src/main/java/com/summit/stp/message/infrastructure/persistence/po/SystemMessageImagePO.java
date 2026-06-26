package com.summit.stp.message.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import nonapi.io.github.classgraph.json.Id;

@Data
@Builder
@TableName("system_message_image")
public class SystemMessageImagePO {
    @TableId(type = IdType.INPUT)
    private Long id;
    private Long messageId;
    private String image;
    private String status;
    private String createTime;
    private String updateTime;
}
