package com.summit.stp.userAuth.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user")
public class UserPO {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField(fill = FieldFill.INSERT)
    private Long publicId;
    private String uname;
    private String password;
    private String phone;
    private Integer statusCode;
    private String gender;
    private Integer age;
    private java.sql.Timestamp createTime;
    private java.sql.Timestamp updateTime;
}
