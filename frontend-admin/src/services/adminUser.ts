import request from '@/services/request'
import type { Result } from '@/types/result'
import type { UserProfileData } from '@/services/user'

export interface PageResult<T> {
  page: number
  total: number
  data: T
}

export interface RangeDTO<T> {
  min?: T | null
  max?: T | null
}

export interface AdminUserQueryPayload {
  page: number
  size: number
  keyword?: string | null
  createTime?: RangeDTO<string> | null
  enabledVIP?: boolean | null
  VIPLevel?: RangeDTO<number> | null
  fans?: RangeDTO<number> | null
  topic?: RangeDTO<number> | null
  follow?: RangeDTO<number> | null
  age?: RangeDTO<number> | null
  gender?: string | null
  phone?: string | null
  ip?: string | null
  statusCode?: number | null
}

export class AdminUserAPI {
  /**
   * 条件分页查询用户列表
   */
  static async list(payload: AdminUserQueryPayload): Promise<Result<PageResult<UserProfileData[]>>> {
    return await request.post('/a/user/list', payload)
  }

  /**
   * 封禁指定用户
   */
  static async ban(uid: string | number): Promise<Result<void>> {
    return await request.get('/a/user/ban', { params: { uid } })
  }

  /**
   * 解封指定用户
   */
  static async unban(uid: string | number): Promise<Result<void>> {
    return await request.get('/a/user/unban', { params: { uid } })
  }
}
