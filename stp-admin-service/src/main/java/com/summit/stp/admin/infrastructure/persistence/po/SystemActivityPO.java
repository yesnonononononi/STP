package com.summit.stp.admin.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("system_activity")
public class SystemActivityPO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String type;
    private String module;
    private String title;
    private String content;
    private String targetUrl;
    private LocalDateTime createTime;
}
