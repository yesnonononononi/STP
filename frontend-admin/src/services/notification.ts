import request from '@/services/request'
import type { Result, PageResult } from '@/types/result'

export interface AdminNotificationVO {
  id: number
  fromUserId?: number
  images?: string[]
  content: string
  status: number // 0: 待发布/草稿, 1: 已发布, 2: 已撤回/撤销
  associateUser?: number
  type?: number // noticeType
  publicTime?: string
  createTime?: string
  updateTime?: string
}

export interface NotificationQueryPayload {
  page: number
  size: number
  keyword?: string
  noticeType?: number
  excludeDeleted?: boolean
}

export interface CreateNotificationPayload {
  title?: string
  content: string
  noticeType?: number
  targetType?: number // 1: 全员广播, 2: 指定用户
  targetUserId?: number
  imageUrls?: string[]
}

export class NotificationAPI {
  static async getNotificationList(payload: NotificationQueryPayload): Promise<Result<PageResult<AdminNotificationVO[]>>> {
    return request.post('/a/notification/list', payload)
  }

  static async createNotification(data: CreateNotificationPayload): Promise<Result<void>> {
    return request.post('/a/notification/create', data)
  }

  static async publishNotification(id: number | string): Promise<Result<void>> {
    return request.post(`/a/notification/public/${id}`)
  }

  static async revokeNotification(id: number | string): Promise<Result<void>> {
    return request.post(`/a/notification/revoke/${id}`)
  }

  static async deleteNotification(id: number | string): Promise<Result<void>> {
    return request.post(`/a/notification/delete/${id}`)
  }
}
