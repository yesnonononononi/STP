import request from '@/services/request'
import type { Result } from '@/types/result'

export interface AdminCommentVO {
  id: number
  rootId?: number
  publisherId?: number
  parentId?: number
  postId?: string
  postStatus?: number
  type?: number
  content: string
  createTime: string
  replyCount?: number
  isTop?: number
  status: number // 0: 已屏蔽(BAN), 1: 正常(NORMAL), 2: 已举报(REPORT)
  reportReason?: string
  likeCount?: number
  updateTime?: string
  ipLocation?: string
  clientType?: string
}

export interface CommentQueryPayload {
  page: number
  pageSize: number
  keyword?: string
  status?: number // 0: 已屏蔽(BAN), 1: 正常(NORMAL), 2: 已举报(REPORT)
}

export interface PageResult<T> {
  page?: number
  total?: number
  data?: T
}

export class CommentAPI {
  static async getCommentList(payload: CommentQueryPayload): Promise<Result<PageResult<AdminCommentVO[]>>> {
    return request.post('/a/comment/list', payload)
  }

  static async banComment(id: number): Promise<Result<void>> {
    return request.post(`/a/comment/ban?id=${id}`)
  }

  static async unbanComment(id: number): Promise<Result<void>> {
    return request.post(`/a/comment/unban?id=${id}`)
  }

  static async ignoreReport(id: number): Promise<Result<void>> {
    return request.post(`/a/comment/report/ignore/${id}`)
  }
}
