import type {PayResponse} from '@/services/payment'

export class PayFormBuilder {
  static submitPayment(payData: PayResponse | string): void {
    if (typeof payData === 'string') {
      if (payData.startsWith('http://') || payData.startsWith('https://') || payData.startsWith('/')) {
        window.location.href = payData
      } else {
        const div = document.createElement('div')
        div.innerHTML = payData
        document.body.appendChild(div)
        const form = div.querySelector('form')
        if (form) {
          form.submit()
        }
      }
      return
    }
    if (payData && payData.to) {
      window.location.href = payData.to
    }
  }
}
