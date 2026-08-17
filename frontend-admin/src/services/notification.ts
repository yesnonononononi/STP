import request from '@/services/request'
import type { Result } from '@/types/result'

export interface NotificationItem {
  id: number
  title: string
  content: string
  targetType: 'ALL' | 'SPECIFIC_USER' | 'MEMBER_ONLY' // 接收范围
  targetUserId?: number
  noticeType: 'SYSTEM' | 'ACTIVITY' | 'MAINTENANCE'
  status: number // 1:已发送/有效 0:已撤回/作废
  senderName: string
  sendTime: string
  createTime: string
}

export interface NotificationQueryPayload {
  page: number
  size: number
  keyword?: string
  noticeType?: string
  targetType?: string
  status?: number
}

export class NotificationAPI {
  static async getNotificationList(payload: NotificationQueryPayload): Promise<Result<{ list: NotificationItem[]; total: number }>> {
    return request.post('/a/notification/list', payload)
  }

  static async createNotification(data: Partial<NotificationItem>): Promise<Result<void>> {
    return request.post('/a/notification/create', data)
  }

  static async updateNotification(data: Partial<NotificationItem>): Promise<Result<void>> {
    return request.post('/a/notification/update', data)
  }

  static async deleteNotification(id: number): Promise<Result<void>> {
    return request.post(`/a/notification/delete/${id}`)
  }

  static async revokeNotification(id: number): Promise<Result<void>> {
    return request.post(`/a/notification/revoke/${id}`)
  }
}
