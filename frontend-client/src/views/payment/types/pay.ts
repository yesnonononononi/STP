import type {Result} from '@/types/result'

export interface pay {
  pay(form: payForm): Promise<Result<payResponse>>
}

export interface payForm {
  uname: string
  packageId: string
  couponId: number | null
  quantity: number
  payType: number
}

export interface payResponse {
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
