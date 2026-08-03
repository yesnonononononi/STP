import type { Result } from '@/types/result'
import request from '@/services/request'

export interface SignInInfoData {
  checkedDates: number[]
  consecutiveDays: number
  todayChecked: boolean
  monthCheckedCount: number
}

export class ToolBoxAPI {
  /**
   * 获取当前用户的打卡状态
   */
  static async getSignInStatus(month?: number): Promise<Result<SignInInfoData>> {
    return await request.get(`/toolbox/signin/status/${month ? month : new Date().getMonth() + 1}`)
  }

  /**
   * 执行每日签到打卡
   */
  static async doSignIn(): Promise<Result<void>> {
    return await request.post('/toolbox/signin')
  }
}
