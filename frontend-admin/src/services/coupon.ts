import request from '@/services/request'
import type { Result, PageResult } from '@/types/result'

export interface AdminCouponVO {
  id: number
  name: string
  discount?: number
  amount?: number
  type?: number // 0 折扣, 1 金额
  status?: number
  scopeType?: number
  timeType?: number
  createTime?: string
  updateTime?: string
  validDays?: number
  validHours?: number
  image?: string
  description?: string
}

export interface CreateCouponPayload {
  id?: number
  name: string
  discount?: number
  amount?: number
  status?: number
  type?: number // 0 折扣, 1 金额
  description?: string
  image?: string
  scopeType?: number
  scopeRelationIds?: number[]
  timeType?: number
  validDays?: number
  validHours?: number
}

export interface AdminCouponActivityVO {
  id: number
  couponId: number
  name: string
  stock?: number
  activityStartTime?: string
  activityEndTime?: string
  status?: number
  type?: number
}

export interface CreateCouponActivityPayload {
  id?: number
  couponId: number
  name: string
  stock?: number
  activityStartTime?: string
  activityEndTime?: string
  status?: number
  type?: number
}

export class CouponAPI {
  // --- 优惠券管理 ---
  static async getCouponList(params?: { keyword?: string; status?: number; page?: number; pageSize?: number }): Promise<Result<PageResult<AdminCouponVO[]>>> {
    return request.get('/a/coupon/list', { params })
  }

  static async saveCoupon(data: CreateCouponPayload): Promise<Result<void>> {
    return request.post('/a/coupon/save', data)
  }

  static async banCoupon(id: number): Promise<Result<void>> {
    return request.post('/a/coupon/ban', null, { params: { id } })
  }

  static async unbanCoupon(id: number): Promise<Result<void>> {
    return request.post('/a/coupon/unban', null, { params: { id } })
  }

  static async deleteCoupon(id: number): Promise<Result<void>> {
    return request.post(`/a/coupon/delete/${id}`)
  }

  // --- 营销活动管理 ---
  static async getActivityList(params?: { keyword?: string; status?: number; page?: number; pageSize?: number }): Promise<Result<PageResult<AdminCouponActivityVO[]>>> {
    return request.get('/a/coupon/activity/list', { params })
  }

  static async saveActivity(data: CreateCouponActivityPayload): Promise<Result<void>> {
    return request.post('/a/coupon/activity/save', data)
  }

  static async startActivity(id: number): Promise<Result<void>> {
    return request.post('/a/coupon/activity/start', null, { params: { id } })
  }

  static async closeActivity(id: number): Promise<Result<void>> {
    return request.post('/a/coupon/activity/close', null, { params: { id } })
  }

  static async deleteActivity(id: number): Promise<Result<void>> {
    return request.post(`/a/coupon/activity/delete/${id}`)
  }
}
