import type { Auth } from '@/shared/types/Auth'
import type { Result } from '@/shared/types/result'
import type { User } from '@/shared/types/user'

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
