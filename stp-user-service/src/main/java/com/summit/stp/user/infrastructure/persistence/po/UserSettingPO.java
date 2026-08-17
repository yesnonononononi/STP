package com.summit.stp.user.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.sql.Timestamp;

@Data
@TableName("user_setting")
public class UserSettingPO {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("show_delPost")
    private Integer showDelPost;

    @TableField("Customization_recommend")
    private Integer customizationRecommend;

    @TableField("create_time")
    private Timestamp createTime;

    @TableField("update_time")
    private Timestamp updateTime;
}
