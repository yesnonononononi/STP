package com.summit.stp.user.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Timestamp;

@Data
@TableName("user")
public class UserPO {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String uname;
    private String nick;
    private String password;
    private String phone;
    private String ip;
    private String email;
    private String avatar;
    private String introduction;
    private Integer statusCode;
    private Integer gender;
    private Integer admin;
    @TableField(value = "bg_image", updateStrategy = FieldStrategy.IGNORED)
    private String bgImage;
    private Integer age;
    private Timestamp createTime;
    private Timestamp updateTime;
}
