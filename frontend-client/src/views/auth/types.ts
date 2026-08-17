import type {Result} from '@/types/result'

export interface Auth {
  token: string
  refreshToken: string
  uid: string
  username?: string
  expireTime?: number
}

export interface LoginForm {
  username: string
  password: string
}
export interface RegisterForm {
  phoneNumber: string
  verifyCode: string
  password: string
}
export interface RefreshTokenForm {
  refreshToken: string
  username: string
}
export interface ForgetForm {
  phoneNumber: string
  verifyCode: string
  password: string
  resetType?: number
}

export interface UserAuth {
  login(form: LoginForm): Promise<Result<Auth>>
  refreshToken(form: RefreshTokenForm): Promise<Result<Partial<Auth>>>
  logout(): Promise<Result>
  register(form: RegisterForm): Promise<Result>
  forget(form: ForgetForm): Promise<Result>
}

export class LoginException extends Error {
  constructor(message: string = '登录异常') {
    super(message)
    this.name = 'LoginException'
  }
}

export class TokenRefreshException extends Error {
  constructor(message: string = '登录状态已失效') {
    super(message)
    this.name = 'TokenRefreshException'
  }
}
