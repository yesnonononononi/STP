package com.summit.stp.toolbox.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户签到流水PO对象
 */
@Data
@TableName("user_sign_log")
public class UserSignLogPO {
    /**
     * 自增主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 签到日期（格式：2026-07-17）
     */
    private LocalDate signDate;

    /**
     * 签到具体时间戳
     */
    private LocalDateTime signTime;

    /**
     * 签到来源：1-APP 2-H5 3-小程序
     */
    private Integer signSource;

    /**
     * 本次签到获得的基础积分
     */
    private Integer rewardPoints;

    /**
     * 签到时的连续天数快照（用于历史对账）
     */
    private Integer continuousDaysSnapshot;

    /**
     * 扩展字段（存放补签卡ID、活动ID等）
     */
    private String extra;
}
