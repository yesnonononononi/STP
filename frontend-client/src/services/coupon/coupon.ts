import type {Result} from '@/types/result'
import request from '@/services/request'
import type {CouponStatus} from './type'

export interface CouponVO {
  id: string
  name: string
  amount: number | null
  discount: number | null
  status: number
  createTime: string
  updateTime: string
  endTime?: string
  isAvailable: boolean
  reason: string | null
  scopeType: number
  type: number
  description: string
  startTime?: string
  validDays?: number
  validHours?: number
  endTimeShow?: string
  scopeDescription?: string
  timeType: number
  more: boolean
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
  static getCouponHistory(
    page: number,
    pageSize: number,
    status: CouponStatus | null,
  ): Promise<Result<any>> {
    return request.get('/coupon/history', { params: { page, pageSize, status } })
  }

  /**
   * 根据商品分类ID和具体套餐ID获取用户所有优惠券并标识本单是否可用
   *
   * @param typeId 商品分类ID (对应请求中的 productId)
   * @param packageId 具体套餐ID (对应请求中的 packageId)
   * @return 包含可用性标识的优惠券列表
   */
  /**
   * 根据商品分类ID和具体套餐ID获取用户所有优惠券并标识本单是否可用
   *
   * @param typeId 商品分类ID (对应请求中的 productId)
   * @param packageId 具体套餐ID (对应请求中的 packageId)
   * @return 包含可用性标识的优惠券列表
   */
  static async getCouponsForOrder(
    typeId: number | string,
    packageId: number | string,
  ): Promise<Result<CouponVO[]>> {
    return await request.get('/coupon/order-list', { params: { typeId, packageId } })
  }

  /**
   * 获取所有可领取的优惠券活动列表
   */
  static getCouponActivitiesByScopeType(
    scopeType: number = 1,
  ): Promise<Result<CouponActivityVO[]>> {
    return request.get(`/coupon/activity/list/${scopeType}`)
  }

  /**
   * 用户根据活动ID领取优惠券
   */
  static receiveCoupon(activityId: number | string): Promise<Result<void>> {
    return request.get(`/coupon/activity/receive/${activityId}`)
  }
}

export interface CouponActivityVO {
  id: string
  couponId: string
  name: string
  stock: number
  activityStartTime: string
  activityEndTime: string
  status: number
  limitQuantity?: number
  couponName: string
  discount: number | null
  amount: number | null
  couponType: number // 0折扣, 1金额
  scopeType: number
  description: string
  timeType?: number
  validDays?: number
  validHours?: number
  scopeDescription?: string
  available?: boolean
  isAvailable?: boolean
  image?: string
  type: number
  createTime?: string
  endTimeShow?: string
  isSeckill?: boolean
}
