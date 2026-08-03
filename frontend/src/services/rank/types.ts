import type {PostVO} from '@/services/post/types'

export interface CreatorRankVO {
  id?: number | string
  entityId: number | string
  rank: number
  entityInfo: {
    nick?: string
    avatar?: string
    vipConfigIcon?: string
    ip?: string
  }
  name: string
  score: number
  entityName: string
  week_start_date: string
  type: string
  bgImg?: string
}

export interface PostRankVO {
  id?: number | string
  entityId: number | string
  rank: number
  entityInfo: PostVO
  name: string
  score: number
  entityName: string
  week_start_date: string
  type: string
  bgImg?: string
}

export interface TopicRankVO {
  id?: number | string
  entityId: number | string
  rank: number
  entityInfo: {
    id: number | string
    tagName: string
    useCount?: number
  }
  name: string
  score: number
  entityName: string
  week_start_date: string
  type: string
  bgImg?: string
}

export interface MappedCommonRank {
  id: number | string
  sort: number
  title: string
  count: number
}

export interface MappedCreatorRank {
  id: number | string
  sort: number
  score: number
  nickname: string
  avatar: string
  decoration: string
  ip: string
}
