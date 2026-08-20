package com.summit.stp.admin.application.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Timestamp;

/**
 * 获取仪表盘右侧显示的系统实时告警、大额充值交易与高风险待审核事件。
 * associated table : system_activity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardActivityVO {
    private Long id;
    private String type;
    private String title;
    private String content;
    private Timestamp createTime;
    private String targetUrl;
}
