import type {Result} from '@/types/result'
import type {Auth, ForgetForm, LoginForm, RefreshTokenForm, RegisterForm, UserAuth} from '../types'
import {LoginException, TokenRefreshException} from '../types'
import request from '@/services/request'
import {useUserInfoStore} from '@/stores/userInfo'
import {CommonAPI} from '@/services/common/api'

export class Auther implements UserAuth {
  user = useUserInfoStore()

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
    // 刷新请求走 common 的无拦截器方法，避免认证模块与 request 拦截器互相递归。
    const res = await CommonAPI.refreshToken(form)
    if (!res) throw new TokenRefreshException()
    return res as Result<Partial<Auth>>
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
