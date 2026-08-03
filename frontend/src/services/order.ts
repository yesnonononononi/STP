import type {Result} from '@/types/result'
import request from '@/services/request'

export interface OrderQueryVO {
  orderId: string | number
  amount: string | number // 实付金额
  status: number
  payTypeName: string
  createTime: string
  toName: string
  payableAmount: string | number // 应付金额
  memberName: string
  memberId: number | null
  couponName: string
  couponId: number | null
  payTime?: string | null
}

export class OrderAPI {
  /**
   * 根据订单ID查询订单详情
   */
  static async queryOrder(orderId: string | number): Promise<Result<OrderQueryVO>> {
    return await request.post(`/order/query/${orderId}`)
  }

  /**
   * 根据订单ID删除订单
   */
  static async deleteOrder(orderId: string | number): Promise<Result<void>> {
    return await request.post(`/order/delete/${orderId}`)
  }

  /**
   * 分页查询历史订单列表
   */
  static async queryHistory(
    page: number = 1,
    pageSize: number = 10,
  ): Promise<Result<OrderQueryVO[]>> {
    return await request.post('/order/query/history', null, {
      params: { page, pageSize },
    })
  }


  /**
   * 手动对账订单并更新/补偿本地权益与状态
   */
  static async reconcileOrder(orderId: string | number): Promise<Result<string>> {
    return await request.post(`/order/reconcile/${orderId}`)
  }

  /**
   * 创建新订单并返回支付参数
   */
  static async createOrder(form: {
    packageId: number
    payType: number
    quantity: number
    uname: string
    couponId: number | null
  }): Promise<Result<any>> {
    return await request.post('/order/create', form)
  }
}
