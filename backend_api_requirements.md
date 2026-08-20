# STP 管理端重构——后端需提供接口契约规范文档 (API Specification)

本文档由前端根据管理端全盘重构需求整理生成，包含了 **仪表盘与系统大盘**、**会员管理**、**优惠券及活动管理**、**帖子管理**、**评论管理**、**订单管理** 及 **系统通知管理** 7 大模块所需后端服务提供的高级管理 API 规范。所有接口标准统一基于 `Result<T>` 协议封装返回。

---

## 0. 仪表盘与系统大盘模块 (Dashboard Management - 新增)

### 0.1 核心 KPI 概况数据
- **URL**: `GET /a/dashboard/overview`
- **说明**: 获取仪表盘顶部 4 个核心 KPI 指标数据，包含总数、今日新增以及同比/环比增长率。
- **响应数据格式 (`Result<DashboardOverviewVO>`)**:
```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "totalUsers": 128560,
    "todayNewUsers": 1420,
    "userGrowthRate": 12.5,
    "totalGmv": 894520.00,
    "todayGmv": 34500.00,
    "gmvGrowthRate": 18.2,
    "activeMembers": 15420,
    "todayNewMembers": 185,
    "memberGrowthRate": 8.7,
    "pendingReports": 14,
    "todayProcessedReports": 86,
    "reportGrowthRate": -15.4
  }
}
```

### 0.2 全站业务趋势图表数据 (GMV、订单数与用户数走势)
- **URL**: `GET /a/dashboard/trends`
- **Params**: `period` (可选: `7d` | `30d` | `year`，默认 `30d`)
- **说明**: 获取特定时间范围内的每日订单数、GMV成交额及新增用户趋势曲线。
- **响应数据格式 (`Result<DashboardTrendsVO>`)**:
```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "dates": ["2026-08-01", "2026-08-02", "2026-08-03", "..."],
    "gmvList": [12400.5, 15800.0, 14200.0, "..."],
    "orderCountList": [320, 410, 380, "..."],
    "newUserCountList": [110, 145, 130, "..."]
  }
}
```

### 0.3 会员等级与消费分布统计
- **URL**: `GET /a/dashboard/member-stats`
- **说明**: 获取当前各个会员等级（如普通会员、白银VIP、黄金VIP、钻石VIP）的用户数量分布比例及各类套餐销售额统计。
- **响应数据格式 (`Result<DashboardMemberStatsVO>`)**:
```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "levelDistribution": [
      { "levelName": "普通会员", "count": 85400, "percentage": 66.4 },
      { "levelName": "白银 VIP", "count": 25600, "percentage": 19.9 },
      { "levelName": "黄金 VIP", "count": 12400, "percentage": 9.6 },
      { "levelName": "钻石 VIP", "count": 5160, "percentage": 4.1 }
    ],
    "packageSalesRatio": [
      { "packageName": "VIP 连续包月", "salesAmount": 458000.0, "count": 18320 },
      { "packageName": "VIP 季卡", "salesAmount": 286000.0, "count": 3575 },
      { "packageName": "VIP 尊享年卡", "salesAmount": 642000.0, "count": 3210 }
    ]
  }
}
```

### 0.4 社区内容活跃度统计 (帖子 vs 评论 vs 违规数)
- **URL**: `GET /a/dashboard/content-stats`
- **Params**: `period` (可选: `7d` | `30d`, 默认 `7d`)
- **说明**: 统计指定周期的每日发帖量、评论互动量以及系统/人工拦截的违规内容数。
- **响应数据格式 (`Result<DashboardContentStatsVO>`)**:
```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "dates": ["08-14", "08-15", "08-16", "08-17", "08-18", "08-19", "08-20"],
    "postCountList": [1420, 1680, 1550, 1890, 2100, 1950, 1780],
    "commentCountList": [5400, 6200, 5800, 7100, 8300, 7600, 6900],
    "blockedCountList": [32, 45, 28, 51, 39, 42, 25]
  }
}
```

### 0.5 微服务系统运行健康度与性能指标
- **URL**: `GET /a/dashboard/system-status`
- **说明**: 获取 STP 各微服务（Gateway, Auth, User, Member, Post, Order, Payment 等）的状态、JVM/CPU 使用率、Redis 命中率及 MQ 积压情况。
- **响应数据格式 (`Result<DashboardSystemStatusVO>`)**:
```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "gatewayQps": 3420,
    "avgResponseTimeMs": 14,
    "cpuUsage": 28.5,
    "memoryUsage": 62.4,
    "redisHitRate": 98.6,
    "mqBacklogCount": 12,
    "services": [
      { "name": "stp-gateway", "status": "UP", "instances": 2, "uptime": "15d 8h" },
      { "name": "stp-auth-service", "status": "UP", "instances": 2, "uptime": "15d 8h" },
      { "name": "stp-user-service", "status": "UP", "instances": 3, "uptime": "15d 8h" },
      { "name": "stp-post-service", "status": "UP", "instances": 3, "uptime": "15d 8h" },
      { "name": "stp-order-service", "status": "UP", "instances": 2, "uptime": "15d 8h" },
      { "name": "stp-payment-service", "status": "UP", "instances": 2, "uptime": "15d 8h" },
      { "name": "stp-im-service", "status": "UP", "instances": 2, "uptime": "15d 8h" }
    ]
  }
}
```

### 0.6 仪表盘最新预警与系统日志列表
- **URL**: `GET /a/dashboard/recent-activities`
- **Params**: `limit` (默认 10)
- **说明**: 获取仪表盘右侧显示的系统实时告警、大额充值交易与高风险待审核事件。
- **响应数据格式 (`Result<List<DashboardActivityVO>>`)**:
```json
{
  "code": 1,
  "msg": "success",
  "data": [
    {
      "id": 101,
      "type": "WARN", // WARN: 风险预警, INFO: 系统日志, ORDER: 大额交易, AUDIT: 待审件
      "title": "检测到高频发帖违规行为",
      "content": "用户 ID: 10092 在 1 分钟内连续发布 15 条重复推广广告",
      "createTime": "2026-08-20 13:20:15",
      "targetUrl": "/post"
    },
    {
      "id": 102,
      "type": "ORDER",
      "title": "大额会员充值成功",
      "content": "用户 ID: 10854 购买了 [VIP 尊享年卡]，金额 ￥198.00",
      "createTime": "2026-08-20 13:15:02",
      "targetUrl": "/order"
    }
  ]
}
```

---

- 
