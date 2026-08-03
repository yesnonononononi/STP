import type { UserSimpleData } from '@/services/user'

export interface CommentItem {
  likeCount: number
  isLike: boolean
  authorIsPraised?: boolean
  authorIsReplied?: boolean
  replyCount?: number
}

export interface CommentAudioMoment {
  audioUrl: string
  audioName: string
  duration: number
  typeCode: number
}

export interface CommentImageMoment {
  imageUrl: string
  imageName: string
  width: number
  height: number
  typeCode: number
  sortOrder?: number
  size?: number
}

export interface CommentExtra {
  mediaType?: number
  imageMoments?: CommentImageMoment[]
  mediaUrl?: string
  audioMoment?: CommentAudioMoment
}

export interface CommentVO {
  id: string
  rootId?: string
  parentId?: string
  postId?: string
  postPublisherId?: string
  postStatus?: boolean
  isAudit?: boolean
  type: number
  content: string
  createTime: string
  publisher: UserSimpleData
  isTop: number
  status: number
  ipLocation?: string
  clientType?: string
  item: CommentItem
  extra: CommentExtra
}

export interface CreateCommentRequest {
  postId: string
  rootId?: string
  parentId?: string
  content: string
  type: number
  extra?: CommentExtra
}

export interface CommentReplyQueryRequest {
  cursor?: string
  postId: string
  rootId: string
  limit: number
}
