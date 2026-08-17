import request from '@/services/request'
import type { Result } from '@/types/result'

export interface OrderItem {
  id: number
  orderNo: string
  userId: number
  userName: string
  orderType: 'MEMBER' | 'COUPON_PACKAGE' | 'OTHER'
  amount: number
  payType: 'WECHAT' | 'ALIPAY' | 'BALANCE'
  payStatus: 'PAID' | 'UNPAID' | 'REFUNDED' | 'FAILED'
  tradeNo?: string
  remark?: string
  createTime: string
  payTime?: string
}

export interface OrderQueryPayload {
  page: number
  size: number
  orderNo?: string
  userId?: number
  orderType?: string
  payStatus?: string
  createTimeStart?: string
  createTimeEnd?: string
}

export class OrderAPI {
  static async getOrderList(payload: OrderQueryPayload): Promise<Result<{ list: OrderItem[]; total: number }>> {
    return request.post('/a/order/list', payload)
  }

  // 补单（针对支付成功但状态未同步的订单）
  static async reissuanceOrder(orderNo: string): Promise<Result<void>> {
    return request.post('/a/order/reissuance', { orderNo })
  }

  // 退单（触发退款/撤销）
  static async refundOrder(orderNo: string, reason: string): Promise<Result<void>> {
    return request.post('/a/order/refund', { orderNo, reason })
  }

  // 订单状态核对（向三方/渠道校验订单实时状态）
  static async verifyOrderStatus(orderNo: string): Promise<Result<{ orderNo: string; syncedStatus: string; message: string }>> {
    return request.post('/a/order/verify-status', { orderNo })
  }
}
