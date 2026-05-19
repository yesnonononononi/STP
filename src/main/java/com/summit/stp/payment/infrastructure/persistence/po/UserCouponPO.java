package com.summit.stp.payment.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Timestamp;

@Data
@TableName("user_coupon")
public class UserCouponPO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long couponId; // 数据库列对应 coupon_id (模板ID)
    private Integer status;
    private Timestamp createTime;
    private Timestamp updateTime;
}
