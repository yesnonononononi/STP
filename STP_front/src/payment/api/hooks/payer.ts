import type { Result } from '@/shared/types/result'

import request from '@/shared/api/request'
import type { pay, payForm, payResponse } from '@/domain/entities/pay'
import { PayFormBuilder } from '@/payment/infrastructure/utils/PayFormBuilder'

export class Payer implements pay {
  async pay(form: payForm): Promise<Result<payResponse>> {
    return await request.post('/order/create', form)
  }

  async getPayTypes(): Promise<Result<string[]>> {
    return await request.get('/pay/types')
  }
}
