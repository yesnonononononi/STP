import request from '@/services/request'
import type { Result } from '@/types/result'

export interface PostItem {
  id: number
  userId: number
  authorName: string
  authorAvatar: string
  title: string
  content: string
  images?: string[]
  tags?: string[]
  topicName?: string
  viewCount: number
  likeCount: number
  commentCount: number
  status: number // 1:审核通过/正常 0:审核不通过/已屏蔽 2:待审核
  rejectReason?: string
  createTime: string
}

export interface PostQueryPayload {
  page: number
  size: number
  keyword?: string
  topicName?: string
  status?: number
  authorId?: number
  createTimeStart?: string
  createTimeEnd?: string
  minLikes?: number
  minComments?: number
}

export class PostAPI {
  static async getPostList(payload: PostQueryPayload): Promise<Result<{ list: PostItem[]; total: number }>> {
    return request.post('/a/post/list', payload)
  }

  static async togglePostStatus(id: number, status: number, rejectReason?: string): Promise<Result<void>> {
    return request.post('/a/post/toggle-status', { id, status, rejectReason })
  }

  static async deletePost(id: number): Promise<Result<void>> {
    return request.post(`/a/post/delete/${id}`)
  }
}
