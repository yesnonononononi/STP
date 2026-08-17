import request from '@/services/request'
import type { Result } from '@/types/result'

export interface CouponItem {
  id: number
  title: string
  couponType: 'CASH' | 'DISCOUNT' // 1:立减券 2:折扣券
  discountValue: number // 减免金额或折扣率(如8.5折)
  minThreshold: number // 使用门槛金额(0表示无门槛)
  totalCount: number // 总发行量
  remainCount: number // 剩余数量
  perUserLimit: number // 每人限领张数
  status: number // 1:正常 0:已封禁/已作废
  validStartTime: string
  validEndTime: string
  createTime: string
}

export interface CouponActivityItem {
  id: number
  activityName: string
  description: string
  associatedCouponId: number
  associatedCouponTitle?: string
  status: number // 1:进行中 0:已暂停 2:已结束
  startTime: string
  endTime: string
  createTime: string
}

export class CouponAPI {
  // --- 优惠券管理 ---
  static async getCouponList(params?: { keyword?: string; status?: number; page?: number; size?: number }): Promise<Result<{ list: CouponItem[]; total: number }>> {
    return request.get('/a/coupon/list', { params })
  }

  static async saveCoupon(data: Partial<CouponItem>): Promise<Result<void>> {
    return request.post('/a/coupon/save', data)
  }

  static async deleteCoupon(id: number): Promise<Result<void>> {
    return request.post(`/a/coupon/delete/${id}`)
  }

  static async toggleCouponStatus(id: number, status: number): Promise<Result<void>> {
    return request.post('/a/coupon/toggle-status', { id, status })
  }

  // --- 活动管理 ---
  static async getActivityList(params?: { keyword?: string; status?: number; page?: number; size?: number }): Promise<Result<{ list: CouponActivityItem[]; total: number }>> {
    return request.get('/a/coupon/activity/list', { params })
  }

  static async saveActivity(data: Partial<CouponActivityItem>): Promise<Result<void>> {
    return request.post('/a/coupon/activity/save', data)
  }

  static async deleteActivity(id: number): Promise<Result<void>> {
    return request.post(`/a/coupon/activity/delete/${id}`)
  }

  static async toggleActivityStatus(id: number, status: number): Promise<Result<void>> {
    return request.post('/a/coupon/activity/toggle-status', { id, status })
  }
}
