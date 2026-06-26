import request from '../request'
import type { PageResult } from '../post'
import type { friendVO } from '../message/message'
import type { Result } from '@/types/result'

export class FriendAPI {
  public static async queryAllFriend(
    uid: string,
    page: number,
    pageSize: number = 10,
  ): Promise<Result<PageResult<friendVO>>> {
    return await request.get(`/relationship/list/${uid}`, {
      params: {
        page: page,
        pageSize: pageSize,
      },
    })
  }
}
