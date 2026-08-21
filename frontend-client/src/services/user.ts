import type { Result } from '@/types/result'
import request from '@/services/request'

export interface UserProfileData {
  id: string
  nick: string
  avatar: string
  gender?: number
  age?: number
  memberLevel: string
  ip: string
  liked: string
  topic: string
  fans: string
  introduction?: string | null
  vipType: string
  phone: string | null
  email: string | null
  vipExpireDate: string
  vipConfigIcon: string
  followed?: boolean
  bgImage: string
}

export interface UserFansData {
  id: string
  userId: string
  nick: string
  avatar: string
  status: number
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
  age?: number | null
  gender?: number | null
  introduction?: string | null
  email?: string
  bgImage?: string | null
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
  static async getUserById(id: string | number): Promise<Result<UserProfileData>> {
    return await request.get(`/user/${id+""}`)
  }

  /**
   * 根据用户ID获取用户简单公开展示信息 (头像、昵称、粉丝、话题数、获赞数)
   */
  static async getSimpleUserById(id: string | number): Promise<Result<UserSimpleData>> {
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

  /**
   * 关注/取消关注用户 (Toggle 开关)
   */
  static async toggleFollow(
    followerId: string | number,
    followeeId: string | number,
    source: string = 'profile',
  ): Promise<Result<void>> {
    return await request.post('/user/follow/follow', { followerId: "" + followerId, followeeId:""+followeeId, source })
  }

  /**
   * 关注用户
   */
  static async followUser(
    followerId: string | number,
    followeeId: string | number,
    source: string = 'profile',
  ): Promise<Result<void>> {
    return await request.post('/user/follow/follow', { followerId: "" + followerId, followeeId:""+followeeId, source })
  }

  /**
   * 取消关注
   * @param id 关注关系主键ID
   */
  static async unfollowUser(id: string | number): Promise<Result<void>> {
    return await request.delete(`/user/follow/unfollow/${id}`)
  }

  /**
   * 分页获取指定用户的粉丝列表（复用后端 /user/follow/followees 接口）
   * @param userId 目标用户ID
   */
  static async getMyFans(
    userId: string | number,
    page: number,
    pageSize: number,
  ): Promise<Result<{ records: UserFansData[]; total: number }>> {
    const res = await request.get('/user/follow/followees', { params: { userId, page, pageSize } })
      // 后端返回 UserFollowVO（id/followerId/followeeId），适配为前端 UserFansData
      return {
        ...res,
        data: {
          records: (res.data.records || []).map((r: any) => ({
            id: r.id,
            userId: r.followerId,
            nick: r.nick || `用户${r.followerId}`,
            avatar: r.avatar || '',
            status: r.status,
          })),
          total: res.data.total || 0,
        },
    }
  }

  /**
   * 获取当前用户偏好设置，如果不存在则自动初始化
   */
  static async getSettings(): Promise<Result<UserSettingData>> {
    return await request.get('/user/setting/current')
  }

  /**
   * 更新当前用户的偏好设置
   */
  static async updateSettings(form: {
    showDelPost: number
    customizationRecommend: number
  }): Promise<Result<void>> {
    return await request.post('/user/setting/update', form)
  }

  /**
   * 举报用户
   */
  static async reportUser(
    reportedId: string | number,
    reason: string,
    evidence?: string[],
  ): Promise<Result<void>> {
    return await request.post('/user/report', { reportedId, reason, evidence: evidence || [] })
  }
}

export interface UserSettingData {
  userId: string | number
  showDelPost: number
  customizationRecommend: number
}
