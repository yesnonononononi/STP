import type { Result } from '@/types/result'
import request from '@/services/request'

export interface UserProfileData {
  id: number
  nick: string
  avatar: string
  gender?: number
  age?: number
  memberLevel: string
  ip: string
  liked: string
  topic: string
  fans: string
  introduction?: string
  vipType: string
  phone: string | null
  email: string | null
  vipExpireDate: string
  vipConfigIcon: string
}

/**
 * 用户公开展示简单信息
 */
export interface UserSimpleData {
  id: string
  nick: string
  avatar: string
  age?: number
  memberLevel: string
  memberLevelName: string
  vipType: string
  vipConfigIcon: string
  ip: string
  introduction?: string | null
  fans?: string | number
  liked?: string | number
  topic?: string | number
  gender?: string
  followed?: boolean
}

/**
 * 修改用户基本资料表单
 */
export interface UserProfileUpdateForm {
  nick?: string
  avatar?: string
  age?: number
  gender?: number
  introduction?: string
  email?: string
  verifyCode?: string
}

/**
 * 手机号绑定表单
 */
export interface UserPhoneBindForm {
  phoneNumber: string
  verifyCode: number
}

/**
 * 修改密码表单
 */
export interface UserPasswordUpdateForm {
  oldPassword: string
  newPassword: string
}

export class UserAPI {
  /**
   * 获取当前登录用户的详细信息
   */
  static async getCurrentUser(): Promise<Result<UserProfileData>> {
    return await request.get('/user/current')
  }

  /**
   * 根据用户ID获取指定用户的详细信息
   */
  static async getUserById(id: number): Promise<Result<UserProfileData>> {
    return await request.get(`/user/${id}`)
  }

  /**
   * 根据用户ID获取用户简单公开展示信息 (头像、昵称、粉丝、话题数、获赞数)
   */
  static async getSimpleUserById(id: number): Promise<Result<UserSimpleData>> {
    return await request.get(`/user/simple/${id}`)
  }

  /**
   * 修改当前用户的基本资料（昵称、头像、邮箱、简介等）
   */
  static async updateProfile(form: UserProfileUpdateForm): Promise<Result<void>> {
    return await request.post('/user/profile/update', form)
  }

  /**
   * 绑定或换绑手机号
   */
  static async bindPhone(form: UserPhoneBindForm): Promise<Result<void>> {
    return await request.post('/user/phone/bind', form)
  }

  /**
   * 修改登录密码
   */
  static async updatePassword(form: UserPasswordUpdateForm): Promise<Result<void>> {
    return await request.post('/user/password/update', form)
  }
}
