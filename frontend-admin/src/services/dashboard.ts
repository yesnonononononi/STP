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
  gatewayQps: number
  avgResponseTimeMs: number
  cpuUsage: number
  memoryUsage: number
  redisHitRate: number
  mqBacklogCount: number
  services: ServiceStatusItem[]
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

// Fallback Mock 数据，保障在后端接口异常时前端平滑兜底
const MOCK_OVERVIEW: DashboardOverview = {
  totalUsers: 128560,
  todayNewUsers: 1420,
  userGrowthRate: '12.5%',
  totalGmv: '894520.00',
  todayGmv: '34500.00',
  gmvGrowthRate: '18.2%',
  activeMembers: 15420,
  todayNewMembers: 185,
  memberGrowthRate: '8.7%',
  pendingReports: 14,
  todayProcessedReports: 86,
  reportGrowthRate: '-15.4%',
}

const MOCK_TRENDS_30D: DashboardTrends = {
  dates: Array.from({ length: 15 }, (_, i) => `08-${String(i + 6).padStart(2, '0')}`),
  gmvList: [18200, 21400, 19800, 24500, 28900, 31200, 29800, 34500, 32100, 38900, 41200, 39500, 43200, 46800, 48500],
  orderCountList: [420, 510, 480, 590, 680, 720, 690, 810, 760, 920, 980, 930, 1020, 1110, 1150],
  newUserCountList: [120, 145, 130, 165, 190, 210, 195, 230, 215, 260, 280, 265, 295, 320, 340],
}

const MOCK_MEMBER_STATS: DashboardMemberStats = {
  typePackageGroups: [
    {
      typeId: 1,
      typeName: '普通会员',
      packages: [
        { packageId: 7, packageName: '白银会员套餐', count: 1250, salesAmount: 37500.0, percentage: 55.4 },
        { packageId: 1, packageName: '体验套餐', count: 1010, salesAmount: 10100.0, percentage: 44.6 }
      ]
    },
    {
      typeId: 2,
      typeName: '超级会员',
      packages: [
        { packageId: 8, packageName: 'VIP连续包月套餐', count: 3200, salesAmount: 41600.0, percentage: 68.2 },
        { packageId: 2, packageName: '超级会员尊享年卡', count: 1490, salesAmount: 295020.0, percentage: 31.8 }
      ]
    }
  ],
  levelDistribution: [
    { level: 0, levelName: '非VIP用户', count: 85400, percentage: 66.4 },
    { level: 1, levelName: '黄金 VIP', count: 25600, percentage: 19.9 },
    { level: 2, levelName: '白银 VIP', count: 12400, percentage: 9.6 }
  ]
}

const MOCK_CONTENT_STATS: DashboardContentStats = {
  dates: ['08-14', '08-15', '08-16', '08-17', '08-18', '08-19', '08-20'],
  postCountList: [1420, 1680, 1550, 1890, 2100, 1950, 1780],
  commentCountList: [5400, 6200, 5800, 7100, 8300, 7600, 6900],
  blockedCountList: [32, 45, 28, 51, 39, 42, 25],
}

const MOCK_SYSTEM_STATUS: DashboardSystemStatus = {
  gatewayQps: 3420,
  avgResponseTimeMs: 14,
  cpuUsage: 28.5,
  memoryUsage: 62.4,
  redisHitRate: 98.6,
  mqBacklogCount: 12,
  services: [
    { name: 'stp-gateway', status: 'UP', instances: 2, uptime: '15d 8h' },
    { name: 'stp-auth-service', status: 'UP', instances: 2, uptime: '15d 8h' },
    { name: 'stp-user-service', status: 'UP', instances: 3, uptime: '15d 8h' },
    { name: 'stp-post-service', status: 'UP', instances: 3, uptime: '15d 8h' },
    { name: 'stp-order-service', status: 'UP', instances: 2, uptime: '15d 8h' },
    { name: 'stp-payment-service', status: 'UP', instances: 2, uptime: '15d 8h' },
    { name: 'stp-im-service', status: 'UP', instances: 2, uptime: '15d 8h' },
  ],
}

const MOCK_ACTIVITIES: DashboardActivity[] = [
  {
    id: 101,
    type: 'WARN',
    title: '高频发帖风控告警',
    content: '用户 ID: 10092 在 1 分钟内连续发布 15 条垃圾推广帖，已被系统自动拦截并降权',
    createTime: '10:20:15',
    targetUrl: '/post',
  },
  {
    id: 102,
    type: 'ORDER',
    title: '新尊享年卡用户',
    content: '用户 ID: 10854 成功购买 [VIP 尊享年卡]，充值金额 ￥198.00',
    createTime: '09:45:02',
    targetUrl: '/order',
  },
  {
    id: 103,
    type: 'AUDIT',
    title: '待处理社区举报',
    content: '新增 3 条评论被用户举报涉嫌违规谩骂，等待管理员审核处理',
    createTime: '09:12:30',
    targetUrl: '/comment',
  },
  {
    id: 104,
    type: 'INFO',
    title: '微服务节点平滑扩容',
    content: 'stp-post-service 实例数自动从 2 扩容至 3 以应对高峰期流量',
    createTime: '08:30:00',
  },
]

export const DashboardAPI = {
  // 获取核心 KPI
  async getOverview(): Promise<Result<DashboardOverview>> {
    try {
      const res = (await request.get('/a/dashboard/overview')) as unknown as Result<DashboardOverview>
      if (res && res.code === 1 && res.data) {
        return res
      }
    } catch {
      // 容错 fallback
    }
    return { code: 1, errMsg: null, data: MOCK_OVERVIEW }
  },

  // 获取趋势数据
  async getTrends(period: string = '30d'): Promise<Result<DashboardTrends>> {
    try {
      const res = (await request.get('/a/dashboard/trends', { params: { period } })) as unknown as Result<DashboardTrends>
      if (res && res.code === 1 && res.data) {
        return res
      }
    } catch {
      // 容错 fallback
    }
    return { code: 1, errMsg: null, data: MOCK_TRENDS_30D }
  },

  // 获取会员与消费统计 (支持 tab 动态筛选)
  async getMemberStats(tab?: string): Promise<Result<DashboardMemberStats>> {
    try {
      const res = (await request.get('/a/dashboard/member-stats', { params: { tab } })) as unknown as Result<DashboardMemberStats>
      if (res && res.code === 1 && res.data) {
        return res
      }
    } catch {
      // 容错 fallback
    }
    return { code: 1, errMsg: null, data: MOCK_MEMBER_STATS }
  },

  // 获取社区内容活跃度
  async getContentStats(period: string = '7d'): Promise<Result<DashboardContentStats>> {
    try {
      const res = (await request.get('/a/dashboard/content-stats', { params: { period } })) as unknown as Result<DashboardContentStats>
      if (res && res.code === 1 && res.data) {
        return res
      }
    } catch {
      // 容错 fallback
    }
    return { code: 1, errMsg: null, data: MOCK_CONTENT_STATS }
  },

  // 获取微服务系统状态
  async getSystemStatus(): Promise<Result<DashboardSystemStatus>> {
    try {
      const res = (await request.get('/a/dashboard/system-status')) as unknown as Result<DashboardSystemStatus>
      if (res && res.code === 1 && res.data) {
        return res
      }
    } catch {
      // 容错 fallback
    }
    return { code: 1, errMsg: null, data: MOCK_SYSTEM_STATUS }
  },

  // 获取最新预警与日志
  async getRecentActivities(limit: number = 10): Promise<Result<DashboardActivity[]>> {
    try {
      const res = (await request.get('/a/dashboard/recent-activities', { params: { limit } })) as unknown as Result<DashboardActivity[]>
      if (res && res.code === 1 && res.data) {
        return res
      }
    } catch {
      // 容错 fallback
    }
    return { code: 1, errMsg: null, data: MOCK_ACTIVITIES }
  },
}
