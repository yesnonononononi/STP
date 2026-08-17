package com.summit.stp.message.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@TableName("system_message")
@Builder
public class SystemMessagePO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long fromUserId;
    private String content;
    private Integer status;
    private Long associateUser;
    private Integer type;
    private Instant publicTime;
    private Instant createTime;
    private Instant updateTime;
}
