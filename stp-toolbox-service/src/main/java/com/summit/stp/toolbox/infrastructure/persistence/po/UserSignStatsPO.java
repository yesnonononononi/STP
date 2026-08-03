package com.summit.stp.toolbox.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户签到统计PO对象
 */
@Data
@TableName("user_sign_stats")
public class UserSignStatsPO {
    /**
     * 用户ID (主键)
     */
    @TableId
    private Long userId;

    /**
     * 历史累计签到总天数
     */
    private Integer totalDays;

    /**
     * 当前连续签到天数（截至今天）
     */
    private Integer currentContinuousDays;

    /**
     * 历史最高连续签到天数（勋章用）
     */
    private Integer maxContinuousDays;

    /**
     * 最后一次签到日期（用于快速判断昨天是否签到）
     */
    private LocalDate lastSignDate;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
