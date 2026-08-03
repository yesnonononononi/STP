import type { UserItem } from '@/types/user'

export interface commentItem {
  id: string
  publisher: UserItem
  content: string
  publishTime: string
  likeCount: string // 点赞数
  isLiked: boolean // 是否点赞
}

