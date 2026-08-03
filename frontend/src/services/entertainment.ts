import type {Result} from '@/types/result'
import request from '@/services/request'

export interface EmojiVO {
  id: string | number
  tiny: string
  name: string
  type: number
  url: string
  packageId?: string | number
}

export interface EmojiPackageVO {
  id: string | number
  name: string
}

export class EntertainmentAPI {
  /**
   * 获取所有有效的表情包
   */
  static getEmojiPackages(): Promise<Result<EmojiPackageVO[]>> {
    return request.get('/entertainment/emoji/packages')
  }

  /**
   * 根据表情包 ID 获取对应的表情列表
   */
  static getEmojiList(packageId: string | number): Promise<Result<EmojiVO[]>> {
    return request.get(`/entertainment/emoji/list/${packageId}`)
  }
}
