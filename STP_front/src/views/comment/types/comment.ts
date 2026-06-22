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
  expandMore?: boolean
  hotScore?: string
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
  replyToUser?: string
  FunctionField?: {
    expandMore: boolean
    replys: CommentVO[]
    loading?: boolean
    hasMore: boolean
    showCount?: number
  }
}

export const init = (vo: CommentVO) => {
  if (!vo.FunctionField) {
    vo.FunctionField = {
      expandMore: false,
      replys: [],
      loading: false,
      hasMore: true,
      showCount: 2,
    }
  }
}
