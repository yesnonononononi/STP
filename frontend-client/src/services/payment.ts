import type {Result} from '@/types/result'
import request from '@/services/request'

export interface PayForm {
  uname: string
  packageId: number
  couponId: number | null
  quantity: number
  payType: number
}

export interface PayResponse {
  pid: number | string
  orderId: number | string
  sign: string
  money: string
  memberName: string | number
  type: string
  signType: string
  to: string
  timestamp: string
  returnUrl: string
  notifyUrl: string
}

export class PayAPI {
  static async pay(form: PayForm): Promise<Result<{ orderId: string | number; endTime?: string | number }>> {
    return await request.post('/order/create', form)
  }

  static async toPay(orderNo: string | number): Promise<Result<string>> {
    return await request.post('/pay/toPay', null, { params: { orderNo } })
  }

  static async getPayTypes(): Promise<Result<string[]>> {
    return await request.get('/pay/types')
  }
}
