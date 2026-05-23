import type { Result } from '@/shared/types/result'

export interface Symbols {
  id: string
  name: string
}
export interface topicTab {
  queryTabListByUId(uid: string): Result<Symbols[]>
  saveTab(symbolId: string): Result<boolean>
  deleteTab(symbolId: string): Result<boolean>
}
