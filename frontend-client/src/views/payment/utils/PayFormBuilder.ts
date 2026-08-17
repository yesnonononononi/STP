import type {PayResponse} from '@/services/payment'

export class PayFormBuilder {
  static submitPayment(payData: PayResponse): void {
    if (payData && payData.to) {
      window.location.href = payData.to
    }
  }
}
