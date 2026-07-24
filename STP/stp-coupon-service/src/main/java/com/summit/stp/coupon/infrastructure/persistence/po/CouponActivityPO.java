package com.summit.stp.coupon.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("coupon_activity")
public class CouponActivityPO {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField(fill = FieldFill.INSERT)
    private Long publicId;
    private Long couponId;
    private String name;
    private Integer stock;
    private LocalDateTime activityStartTime;
    private LocalDateTime activityEndTime;
    private Integer status;
    private Integer limitQuantity;
    private LocalDateTime createTime;
    private Integer type;
    private LocalDateTime updateTime;
}
