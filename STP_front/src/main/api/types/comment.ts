import type { Result } from '@/shared/types/result'
import type { UserItem } from '@/shared/types/user'

export interface commentItem {
  id: string
  publisher: UserItem
  content: string
  publishTime: string
  likeCount: string // 点赞数
  isLiked: boolean // 是否点赞
}
export interface content {
  type: string
  content: string
  title: string
  img: string
  video: string
  audio: string
}

export interface topicItem {
  id: string
  publisher: UserItem
  content: content
  publishTime: string
  likeCount: string // 点赞数
  replyCount: string // 回复数
  isLiked: boolean // 是否点赞
  collectedCount: string //收藏数
  isCollected: boolean // 是否收藏
}

export interface topicDetail extends topicItem {
  commentList: commentItem[]
}
export interface topic {
  queryTopicList(symbelId: string): Result<topicItem[]>
  queryTopicById(topickId: string): Result<topicDetail>
  like(topicId: string): Result<boolean>
  collect(topicId: string): Result<boolean>
}
