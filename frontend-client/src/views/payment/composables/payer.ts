import type {Result} from '@/types/result'
import request from '@/services/request'
import type {pay, payForm, payResponse} from '../types/pay'

export class Payer implements pay {
  async pay(form: payForm): Promise<Result<payResponse>> {
    return await request.post('/order/create', form)
  }

  async getPayTypes(): Promise<Result<string[]>> {
    return await request.get('/pay/types')
  }
}
