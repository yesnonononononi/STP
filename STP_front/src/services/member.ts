import type { Result } from '@/types/result'
import request from '@/services/request'

export interface MemberConfig {
  id: number
  name: string
  price: string
  duration: number
  description: string
}

export interface MemberType {
  id: string
  name: string
  description: string
}

export class MemberAPI {
  static async queryMemberConfig(typeId: string): Promise<Result<MemberConfig[]>> {
    return request.get(`/member/get/type/${typeId}`)
  }

  static async queryMemberType(): Promise<Result<[MemberType]>> {
    return request.get('/member/list')
  }
}
