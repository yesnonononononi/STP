import request from '@/services/request'
import type { Result, PageResult, RangeDTO } from '@/types/result'

export interface PostImageItem {
  id?: number
  postId?: number
  width?:number,
  height?:number
  imageUrl?: string
  sortOrder?: number
}

export interface AdminPostVO {
  id: number | string
  creatorId: number | string
  title: string
  type?: string | number
  content: string
  mediaUrls?: string
  urls?: (PostImageItem | string)[]
  replyCount?: number
  status?: string | number
  createTime?: string
  updateTime?: string
  isTop?: number
  viewCount?: number
  likeCount?: number
  collectCount?: number
  hotScore?: number
  visibleScope?: string | number
}

export interface PostVO {
  id: string | number
  creatorId: string | number
  title: string
  type?: number | string
  content?: string
  mediaUrls?: PostImageItem[] | string | null
  extraMediaUrl?: string | null
  likeCount?: number
  replyCount?: number
  collectCount?: number
  status?: number | string
  createTime?: string
  updateTime?: string
  publisher?: {
    id?: string | number
    nick?: string
    avatar?: string
    ip?: string
  }
  isTop?: number
  viewCount?: number
  visibleScope?: number
}

export interface PostQueryPayload {
  page: number
  pageSize: number
  keyword?: string
  status?: string | number
  creatorId?: string
  postId?: string
  likeCount?: RangeDTO<number>
  comment?: RangeDTO<number>
  createTime?: RangeDTO<string>
  updateTime?: RangeDTO<string>
}

export class PostAPI {
  /**
   * 分页条件查询帖子列表 (/a/post/list)
   */
  static async getPostList(payload: PostQueryPayload): Promise<Result<PageResult<AdminPostVO[]>>> {
    return request.post('/a/post/list', payload)
  }

  /**
   * 查询帖子详情 (/a/post/detail)
   */
  static async getPostDetail(id: string | number, status?: string | number): Promise<Result<PostVO>> {
    return request.get('/a/post/detail', { params: { id, status } })
  }

  /**
   * 审核通过 (/a/post/bypass)
   */
  static async bypass(id: string | number): Promise<Result<void>> {
    return request.post('/a/post/bypass', null, { params: { id } })
  }

  /**
   * 审核不通过 (驳回 /a/post/bypass/not)
   */
  static async bypassNot(id: string | number, reason: string): Promise<Result<void>> {
    return request.post('/a/post/bypass/not', null, { params: { id, reason } })
  }

  /**
   * 封禁帖子 (/a/post/ban)
   */
  static async ban(id: string | number): Promise<Result<void>> {
    return request.post('/a/post/ban', null, { params: { id } })
  }

  /**
   * 解封帖子 (/a/post/unban)
   */
  static async unban(id: string | number): Promise<Result<void>> {
    return request.post('/a/post/unban', null, { params: { id } })
  }
}
