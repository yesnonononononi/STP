package com.summit.stp.member.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Timestamp;

@TableName("member_type")
@Data
public class MemberTypePO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Timestamp createTime;
    private Timestamp updateTime;
    private int status;
    private String description;
    private Integer priority;
}
