package com.summit.stp.coupon.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * 优惠券可用范围关联表持久化实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("coupon_use_scope")
public class CouponUseScopePO {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 优惠券模板ID
     */
    private Long couponId;

    /**
     * 关联的分类或商品ID
     */
    private Long relationId;

    /**
     * 创建时间
     */
    private Timestamp createTime;
}
