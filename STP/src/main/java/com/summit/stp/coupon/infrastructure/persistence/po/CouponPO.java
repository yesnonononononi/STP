package com.summit.stp.coupon.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("coupon")
public class CouponPO {
    /**
     * 优惠券ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 优惠券名称
     */
    private String name;
    /**
     * 优惠折扣
     */
    private BigDecimal discount;
    /**
     * 优惠金额
     */
    private BigDecimal amount;
    /**
     * 优惠券类型: 0折扣, 1金额
     */
    private Integer type;
    /**
     * 优惠券状态
     */
    private Integer status;
    /**
     * 可用范围类型：1-全场通用，2-指定商品分类，3-指定商品
     */
    private Integer scopeType;
    /**
     * 有效时间类型 1-固定时间段 2-领取后生效(按天) 3-领取后生效(按小时)
     */
    private Integer timeType;
    /**
     * 创建时间
     */
    private Timestamp createTime;
    /**
     * 更新时间
     */
    private Timestamp updateTime;
    /**
     * 优惠券有效期有效天数
     */
    private Integer validDays;
    /**
     * 优惠券有效期有效小时数
     */
    private Integer validHours;


    /**
     * 优惠券图片
     */
    private String image;
    /**
     * 优惠券描述
     */
    private String description;




}
