import type {Result} from '@/types/result'
import request from '@/services/request'
import {CommonAPI} from '@/services/common/api'

export interface LoginForm {
  username: string
  password?: string
  code?: string
  loginType: 'password' | 'code'
}

export interface RegisterForm {
  username: string
  pass: string
  checkPass: string
  code: string
}

export interface ForgetForm {
  username: string
  pass: string
  checkPass: string
  code: string
}

export interface RefreshTokenForm {
  username: string
  refreshToken: string
}

export interface AuthData {
  token: string
  refreshToken: string
  username: string
  nick: string
  avatar: string
  isVip: number
  endTime: string
}

export class AuthAPI {
  static async login(form: LoginForm): Promise<Result<AuthData>> {
    return await request.post('/user-auth/login', form)
  }

  static async refreshToken(form: RefreshTokenForm): Promise<Result<Partial<AuthData>>> {
    // 认证刷新接口必须绕过普通 request 拦截器。
    return (await CommonAPI.refreshToken(form)) as Result<Partial<AuthData>>
  }

  static async logout(): Promise<Result<any>> {
    return request.post('/user-auth/logout')
  }

  static async register(form: RegisterForm): Promise<Result> {
    return request.post('/user-auth/register', form)
  }

  static async forget(form: ForgetForm): Promise<Result> {
    return request.post('/user-auth/forget', form)
  }
}
