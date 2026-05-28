import type { Result } from '@/types/result'
import request from '@/services/request'

export interface CouponVO {
  id: string
  name: string
  amount: number | null
  discount: number | null
  status: number
  createTime: string
  updateTime: string
}

export class CouponAPI {
  /**
   * 获取当前用户可用的未使用优惠券
   */
  static getAvailableCoupons(): Promise<Result<CouponVO[]>> {
    return request.get('/coupon/list')
  }

  /**
   * 分页获取当前用户的优惠券历史
   */
  static getCouponHistory(page: number, pageSize: number): Promise<Result<any>> {
    return request.get('/coupon/history', { params: { page, pageSize } })
  }
}
