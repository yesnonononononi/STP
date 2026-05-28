import type { Result } from '@/types/result'
import type { Symbols, topicTab } from '../types/statics'

export class TopicTab implements topicTab {
  queryTabListByUId(uid: string): Result<Symbols[]> {
    throw new Error('Method not implemented.')
  }
  saveTab(symbolId: string): Result<boolean> {
    throw new Error('Method not implemented.')
  }
  deleteTab(symbolId: string): Result<boolean> {
    throw new Error('Method not implemented.')
  }
}
