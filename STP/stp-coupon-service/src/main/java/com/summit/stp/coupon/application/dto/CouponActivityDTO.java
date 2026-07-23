package com.summit.stp.coupon.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 优惠券活动数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponActivityDTO {
    /**
     * 活动ID
     */
    private Long id;
    /**
     * 关联优惠券ID
     */
    private Long couponId;
    /**
     * 活动名称
     */
    private String name;
    /**
     * 活动剩余库存
     */
    private Integer stock;
    /**
     * 活动开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime activityStartTime;
    /**
     * 活动结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime activityEndTime;
    /**
     * 活动状态: 1可用, 0禁用
     */
    private Integer status;

    private Integer limitQuantity;
    /**
     * 活动类型 1普通 2秒杀
     */
    private Integer type;
}
