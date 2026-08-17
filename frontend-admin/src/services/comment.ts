import request from '@/services/request'
import type { Result } from '@/types/result'

export interface CommentItem {
  id: number
  postId: number
  postTitle?: string
  userId: number
  userName: string
  userAvatar: string
  content: string
  reportStatus: number // 0:未被举报 1:已被举报(待处理) 2:举报已处理
  reportCount: number
  reportReason?: string
  status: number // 1:审核通过/正常 0:已屏蔽/违规
  createTime: string
}

export interface CommentQueryPayload {
  page: number
  size: number
  keyword?: string
  postId?: number
  reportStatus?: number // 筛选举报状态
  status?: number
}

export class CommentAPI {
  static async getCommentList(payload: CommentQueryPayload): Promise<Result<{ list: CommentItem[]; total: number }>> {
    return request.post('/a/comment/list', payload)
  }

  static async toggleCommentStatus(id: number, status: number): Promise<Result<void>> {
    return request.post('/a/comment/toggle-status', { id, status })
  }

  static async deleteComment(id: number): Promise<Result<void>> {
    return request.post(`/a/comment/delete/${id}`)
  }

  static async ignoreReport(id: number): Promise<Result<void>> {
    return request.post(`/a/comment/report/ignore/${id}`)
  }
}
