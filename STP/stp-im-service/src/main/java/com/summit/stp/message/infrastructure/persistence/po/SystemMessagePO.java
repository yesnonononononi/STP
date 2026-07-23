package com.summit.stp.message.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@TableName("system_message")
@Builder
public class SystemMessagePO {
    @TableId(type = IdType.INPUT)
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
