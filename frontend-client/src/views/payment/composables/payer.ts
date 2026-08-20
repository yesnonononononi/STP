import type {Result} from '@/types/result'
import request from '@/services/request'
import type {pay, payForm, payResponse} from '../types/pay'

export class Payer {
  async pay(form: payForm): Promise<Result<any>> {
    return await request.post('/order/create', form)
  }

  async toPay(orderNo: string | number): Promise<Result<string>> {
    return await request.post('/pay/toPay', null, { params: { orderNo } })
  }

  async getPayTypes(): Promise<Result<string[]>> {
    return await request.get('/pay/types')
  }
}
