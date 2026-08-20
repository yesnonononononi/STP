import request from '@/services/request'
import type { Result, PageResult } from '@/types/result'

export interface AdminOrderVO {
  id: number
  creatorId: number
  amount: number
  payType?: number // 1: 微信, 2: 支付宝, 3: 余额
  to?: string
  sign?: string
  status?: number // 0: 待支付, 1: 已支付, 2: 已退款, 3: 交易失败/已关闭
  packageId?: number
  quantity?: number
  couponId?: number
  unitPrice?: number
  discountAmount?: number
  createTime?: string
  timeoutTime?: string
  updateTime?: string
  payTime?: string
}

export interface OrderQueryPayload {
  page: number
  size: number
  orderNo?: string
  userId?: number
  orderType?: number
  payStatus?: number
}

export class OrderAPI {
  static async getOrderList(payload: OrderQueryPayload): Promise<Result<PageResult<AdminOrderVO[]>>> {
    return request.post('/a/order/list', null, { params: payload })
  }

  // 补单（针对支付成功但状态未同步的订单）
  static async reissuanceOrder(orderNo: number | string): Promise<Result<void>> {
    return request.post('/a/order/reissuance', null, { params: { orderNo } })
  }

  // 退单（触发退款/撤销）
  static async refundOrder(orderNo: number | string, reason: string): Promise<Result<void>> {
    return request.post('/a/order/refund', null, { params: { orderNo, reason } })
  }

  // 订单状态核对（向三方/渠道校验订单实时状态）
  static async verifyOrderStatus(orderNo: number | string): Promise<Result<string>> {
    return request.post('/a/order/verfiy-status', null, { params: { orderNo } })
  }
}
