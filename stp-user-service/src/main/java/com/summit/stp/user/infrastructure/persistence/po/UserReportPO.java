package com.summit.stp.user.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_report")
public class UserReportPO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long reporterId;
    private Long reportedId;
    private String reason;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
