package com.summit.stp.userAuth.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user")
public class UserPO {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String uname;
    private String password;
    private String phone;
    private Integer statusCode;
    private String gender;
    private Integer age;
    private java.sql.Timestamp createTime;
    private java.sql.Timestamp updateTime;
}
