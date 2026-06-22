import type { Result } from '@/types/result'
import request from '@/services/request'

export interface MemberConfig {
  id: number
  price: string
  name: string
  discount: number
  quantity: number
  duration: number
  typeId: string
  typeName: string
  description?: string
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

  static async queryMemberType(): Promise<Result<MemberType[]>> {
    return request.get('/member/list')
  }

  static async queryMemberById(id: string): Promise<Result<MemberConfig>> {
    return request.get(`/member/get/${id}`)
  }
}
