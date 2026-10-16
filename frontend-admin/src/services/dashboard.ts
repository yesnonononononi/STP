import request from './request'
import type { Result } from '@/types/result'

export interface DashboardOverview {
  totalUsers: number
  todayNewUsers: number
  userGrowthRate: string | number
  totalGmv: string | number
  todayGmv: string | number
  gmvGrowthRate: string | number
  activeMembers: number
  todayNewMembers: number
  memberGrowthRate: string | number
  pendingReports: number
  todayProcessedReports: number
  reportGrowthRate?: string | number
}

export interface DashboardTrends {
  dates: string[]
  gmvList: number[]
  orderCountList: number[]
  newUserCountList: number[]
}

export interface LevelDistItem {
  level?: number
  levelName: string
  count: number
  percentage: number
}

export interface PackageDistItem {
  packageId?: number
  packageName: string
  count: number
  salesAmount: number
  percentage: number
}

export interface TypePackageDistGroup {
  typeId: number
  typeName: string
  packages: PackageDistItem[]
}

export interface DashboardMemberStats {
  typePackageGroups?: TypePackageDistGroup[]
  levelDistribution: LevelDistItem[]
}

export interface DashboardContentStats {
  dates: string[]
  postCountList: number[]
  commentCountList: number[]
  blockedCountList: number[]
}

export interface ServiceStatusItem {
  name: string
  status: 'UP' | 'DOWN' | 'WARN'
  instances: number
  uptime: string
}

export interface DashboardSystemStatus {
  // 后端在指标尚未采集到时会返回 null（未知），前端必须展示 “—”，不得用 0 冒充真实值。
  gatewayQps: number | null
  avgResponseTimeMs: number | null
  cpuUsage: number
  memoryUsage: number
  redisHitRate: number
  mqBacklogCount: number | null
  services: ServiceStatusItem[]
  // 后端降级标记：true 表示网关指标 / MQ 积压等实时指标取数失败（对应字段为 null）。
  degraded?: boolean
}

export interface DashboardActivity {
  id: number
  type: 'WARN' | 'INFO' | 'ORDER' | 'AUDIT' | string
  typeDesc?: string
  module?: string
  title: string
  content: string
  createTime: string
  targetUrl?: string
}

// 说明：此处不再提供任何 Mock/兜底数据。
// 所有接口失败时都会由 request.ts 响应拦截器以真实错误（Result.code !== 1，携带 errMsg）
// reject，页面据此展示错误态或空态；严禁用编造数据冒充成功结果。
export const DashboardAPI = {
  // 获取核心 KPI
  getOverview(): Promise<Result<DashboardOverview>> {
    return request.get('/a/dashboard/overview')
  },

  // 获取趋势数据
  getTrends(period: string = '30d'): Promise<Result<DashboardTrends>> {
    return request.get('/a/dashboard/trends', { params: { period } })
  },

  // 获取会员与消费统计 (支持 tab 动态筛选)
  getMemberStats(tab?: string): Promise<Result<DashboardMemberStats>> {
    return request.get('/a/dashboard/member-stats', { params: { tab } })
  },

  // 获取社区内容活跃度
  getContentStats(period: string = '7d'): Promise<Result<DashboardContentStats>> {
    return request.get('/a/dashboard/content-stats', { params: { period } })
  },

  // 获取微服务系统状态
  getSystemStatus(): Promise<Result<DashboardSystemStatus>> {
    return request.get('/a/dashboard/system-status')
  },

  // 获取最新预警与日志
  getRecentActivities(limit: number = 10): Promise<Result<DashboardActivity[]>> {
    return request.get('/a/dashboard/recent-activities', { params: { limit } })
  },
}
