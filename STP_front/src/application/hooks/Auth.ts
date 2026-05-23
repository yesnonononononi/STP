import type { Result } from '@/shared/types/result'
import type { LoginForm, RegisterForm, UserAuth, RefreshTokenForm, ForgetForm } from '../types/auth'
import request from '@/shared/api/request'
import { TokenRefreshException } from '../../domain/exception/TokenRefreshException'
import { LoginException } from '@/auth/domain/exception/LoginException'
import type { Auth } from '@/shared/types/Auth'

export class Auther implements UserAuth {
  private constructor() {}
  private static instance: Auther | null = null
  static getInstance(): Auther {
    if (!Auther.instance) {
      Auther.instance = new Auther()
    }
    return Auther.instance
  }
  async login(form: LoginForm): Promise<Result<Auth>> {
    const res: Result<Auth> = await request.post('/user-auth/login', form)
    if (!res) throw new LoginException('登录失败')
    return res
  }
  async refreshToken(form: RefreshTokenForm): Promise<Result<Partial<Auth>>> {
    const res: Result<Partial<Auth>> = await request.post('/user-auth/refresh-token', form)
    if (!res) throw new TokenRefreshException()
    return res
  }
  async logout(): Promise<Result<any>> {
    return request.post('/user-auth/logout')
  }
  async register(form: RegisterForm): Promise<Result> {
    const res: Result = await request.post('/user-auth/register', form)
    return res
  }
  async forget(form: ForgetForm): Promise<Result> {
    const res: Result = await request.post('/user-auth/forget', form)
    return res
  }
}
