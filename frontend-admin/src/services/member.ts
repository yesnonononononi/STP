import request from '@/services/request'
import type { Result, PageResult } from '@/types/result'

// 特权明细结构
export interface PrivilegeItem {
  name: string
  description: string
  enabled: boolean
}

// 1. 对应后端 MemberLevelConfigVO
export interface MemberLevelConfigItem {
  id?: number | string
  level: number
  levelName: string
  minRecharge: number
  privilegesJson?: string | PrivilegeItem[] | null
  privileges?: PrivilegeItem[]
  iconUrl: string
  sortOrder: number
}

export interface MemberTypeVO {
  id: number | string
  name: string
  description?: string
}

// 2. 对应后端 MemberVO / MemberCreateRequest
export interface MemberPackageItem {
  id?: number
  name: string
  price: number
  duration: number
  description: string
  typeId: number
  typeName?: string
  stock: number
  discount: number
  dailyRate?: number
  priority: number
  type?: string
  isSuper?: boolean
  status?: number
  createTime?: string
  updateTime?: string
}

// 3. 对应后端 UserMemberVO
export interface UserMemberItem {
  userId: number
  totalRecharge: number
  level?: {
    id?: number | string
    level: number
    levelName: string
    minRecharge: number
    privilegesJson?: string | PrivilegeItem[] | null
    iconUrl: string
    sortOrder: number
  } | null
  memberType?: {
    typeId: number
    typeName: string
    description: string
    status: number
  } | null
  packageTypeId?: number
  expireTime?: string
  levelUpgradeTime?: string
  dailyRate?: number
  createTime?: string
  updateTime?: string
}

export interface MemberConfig {
  pointsRate: number
  checkinBonus: number
  vipMonthlyPrice: number
  vipYearlyPrice: number
  autoRenewDiscount: number
  autoApproveRefund: boolean
}

export interface MemberDecorationItem {
  id: number
  name: string
  type: string
  previewUrl: string
  requiredLevel: number
  validDays: number
  status: number
  createTime?: string
}

export class MemberAPI {
  // --- 1. 等级配置管理 (member_level_config) ---
  static async getLevelConfigList(params?: { page?: number; pageSize?: number }): Promise<Result<PageResult<MemberLevelConfigItem[]>>> {
    const queryParams = { page: 1, pageSize: 10, ...params }
    return request.get('/a/member/level-config/list', { params: queryParams })
  }

  static async saveLevelConfig(data: any): Promise<Result<void>> {
    return request.post('/a/member/level-config/save', data)
  }

  static async updateLevelConfig(data: any): Promise<Result<void>> {
    return request.post('/a/member/level-config/update', data)
  }

  static async deleteLevelConfig(level: number): Promise<Result<void>> {
    return request.post(`/a/member/level-config/delete/${level}`)
  }

  // --- 2. 会员套餐配置管理 (member_package) ---
  static async getPackageList(): Promise<Result<MemberPackageItem[]>> {
    return request.get('/a/member/package/list')
  }

  static async getMemberTypeList(): Promise<Result<MemberTypeVO[]>> {
    return request.get('/member/list')
  }

  static async savePackage(data: MemberPackageItem): Promise<Result<void>> {
    return request.post('/a/member/package/save', data)
  }

  static async deletePackage(id: number): Promise<Result<void>> {
    return request.post(`/a/member/package/delete/${id}`)
  }

  // --- 3. 用户会员记录查询与管控 (user_member) ---
  static async getUserMemberList(params?: { page?: number; pageSize?: number }): Promise<Result<PageResult<UserMemberItem[]>>> {
    const queryParams = { page: 1, pageSize: 10, ...params }
    return request.get('/a/member/user/list', { params: queryParams })
  }

  static async updateUserMemberLevel(uid: number): Promise<Result<void>> {
    return request.post('/a/member/user/update-level', null, { params: { uid } })
  }

  static async extendUserExpire(uid: number, expireTime: string): Promise<Result<void>> {
    return request.post('/a/member/user/extend-expire', null, { params: { uid, expireTime } })
  }

  // --- 辅助前端UI Mock扩展 ---
  static async getConfig(): Promise<Result<MemberConfig>> {
    return request.get('/a/member/config')
  }

  static async updateConfig(data: MemberConfig): Promise<Result<void>> {
    return request.post('/a/member/config/update', data)
  }

  static async getDecorationList(): Promise<Result<MemberDecorationItem[]>> {
    return request.get('/a/member/decoration/list')
  }

  static async saveDecoration(data: Partial<MemberDecorationItem>): Promise<Result<void>> {
    return request.post('/a/member/decoration/save', data)
  }

  static async toggleDecorationStatus(id: number, status: number): Promise<Result<void>> {
    return request.post('/a/member/decoration/toggle-status', { id, status })
  }

  static async deleteDecoration(id: number): Promise<Result<void>> {
    return request.post(`/a/member/decoration/delete/${id}`)
  }
}
