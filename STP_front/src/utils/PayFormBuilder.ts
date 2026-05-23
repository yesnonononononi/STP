import type { PayResponse } from '@/api/payment'

export class PayFormBuilder {
  static submitPayment(payData: PayResponse): void {
    const form = document.createElement('form')
    form.method = 'POST'
    form.action = payData.to
    form.style.display = 'none'

    const fields = {
      pid: String(payData.pid),
      type: payData.type.toLowerCase(),
      out_trade_no: String(payData.orderId),
      return_url: payData.returnUrl,
      notify_url: payData.notifyUrl,
      name: String(payData.memberName || ''),
      money: payData.money,
      timestamp: payData.timestamp,
      sign: payData.sign,
      sign_type: payData.signType,
    }

    Object.entries(fields).forEach(([key, value]) => {
      const input = document.createElement('input')
      input.type = 'hidden'
      input.name = key
      input.value = value as string
      form.appendChild(input)
    })

    document.body.appendChild(form)
    form.submit()
    document.body.removeChild(form)
  }
}
