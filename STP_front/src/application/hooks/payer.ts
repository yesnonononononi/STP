import type { Result } from '@/shared/types/result'
import type { pay, payForm, payResponse } from '../types/pay'
import request from '@/shared/api/request'

export class Payer implements pay {
  async pay(form: payForm): Promise<Result<payResponse>> {
    return request.post('/pay', form)
  }
}
