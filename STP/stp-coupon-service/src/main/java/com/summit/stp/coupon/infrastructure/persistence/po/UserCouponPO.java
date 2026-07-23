package com.summit.stp.coupon.infrastructure.persistence.po;

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
    private Long orderId;
    private Long couponId;
    private Integer status;
    private Timestamp usedTime;
    private Timestamp createTime;
    private Timestamp updateTime;
    private Timestamp endTime;
}
