import type { Result } from '@/shared/types/result'
import type { memberConfig } from '../types/member'
import request from '@/shared/api/request'

export class MemberBase {
  /**
   * 查询套餐列表(id,价格,描述)
   */
  async queryMemberConfig(typeId: string): Promise<Result<memberConfig[]>> {
    return request.get(`/member/get/type/${typeId}`)
  }

  async queryMemberType(): Promise<Result<[{ id: string; name: string; description: string }]>> {
    return request.get('/member/list')
  }
}
