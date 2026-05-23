package com.summit.stp.user.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user")
public class UserPO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String uname;
    private String password;
    private String phone;
    private String avatar;
    private Integer statusCode;
    private java.sql.Timestamp createTime;
    private java.sql.Timestamp updateTime;
}
