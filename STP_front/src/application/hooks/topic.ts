import type { Result } from '@/shared/types/result'
import type { topic, topicDetail, topicItem } from '../types/comment'

export class Topic implements topic {
  queryTopicList(symbelId: string): Result<topicItem[]> {
    return { code: 200, data: [], errMsg: null }
  }
  queryTopicById(topickId: string): Result<topicDetail> {
    throw new Error('Method not implemented.')
  }
  like(topicId: string): Result<boolean> {
    throw new Error('Method not implemented.')
  }
  collect(topicId: string): Result<boolean> {
    throw new Error('Method not implemented.')
  }
}
