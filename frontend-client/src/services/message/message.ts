import type {Result} from '@/types/result'
import request from '../request'
import type {UserProfileData, UserSimpleData} from '../user'

export interface messageVO {
  id: string
  user: UserSimpleData | UserProfileData
  sendTime: string
  content: string
  image: string
  audio: string
  video: string
  type: number
  status: number
  sessionId: string
  sending?: boolean
  failed?: boolean
}

export interface Messages {
  messages: messageVO[]
  hasMore: boolean
  sessionId: string
  cursorId: string
}
export interface SendMessageDto {
  msgId?: string
  receiverId: string
  content: string
  image: string
  audio: string
  video: string
  sendTime: string
  type: number
}
export interface MessageSendVO {
  messageId: string
}
export enum messageType {
  IMAGE = 1,
  TEXT = 2,
  AUDIO = 3,
  VIDEO = 4,
}
export enum messageStatus {
  UNREAD = 1,
  READ = 2,
  WITHDRAWN = 3,
}
export interface sysNotice {
  id: number
  content: string
  publicTime: string
  images: string[]
}
export interface friendVO {
  user: UserSimpleData
  lastMsg?: string
  lastTime?: string
  noDisturb?: boolean
}
export class MessageAPI {
  public static async querySysMessage(): Promise<Result<sysNotice[]>> {
    return await request.get('/message/sys/list')
  }

  public static async sendMessage(form: SendMessageDto): Promise<Result<MessageSendVO>> {
    return await request.post('/message/send', form)
  }

  public static async deleteMessage(messageId: string, receiverId: string) {
    return await request.get(`/message/del/${messageId}`, { params: { receiverId } })
  }

  public static async loadMessageHistoryByFriendId(
    friendId: string,
    cursorId: string | null,
    limit: number,
  ): Promise<Result<Messages>> {
    return await request.get(`/message/history/${friendId}`, { params: { cursorId, limit } })
  }

  public static async readAll() {
    return await request.get(`/message/read/all`)
  }

  public static async read(sessionId: string) {
    return await request.get(`/message/read/${sessionId}`)
  }

  public static async loadInteractionMessages(
    lastPublicId: string | null,
    limit: number,
  ): Promise<Result<InteractionMessageVO[]>> {
    return await request.get(`/message/interaction/list`, { params: { lastPublicId, limit } })
  }

  public static async deleteInteractionMessage(id: string): Promise<Result<void>> {
    return await request.delete(`/message/interaction/delete/${id}`)
  }
}

export interface InteractionMessageVO {
  id: string
  senderId: string
  senderAvatar: string
  senderName: string
  receiverId: string
  messageType: number
  content: string
  associateContent: string
  createTime: string
  associateContentTitle: string
  postId: string
  isLike?: boolean
}

export interface SessionVO {
  id: string
  userId: string
  targetId: string
  type: number
  lastMessageId: string
  lastMessageContent: string
  lastSenderId: string
  lastTime: string
  unreadCount: number
  isTop: number
  isMute: number
  draft: string
  isHidden: number
  targetNickName: string
  targetAvatar: string
}

export class SessionAPI {
  public static async querySessionList(): Promise<Result<SessionVO[]>> {
    return await request.get('/session/list')
  }

  public static async saveSession(form: {
    targetId: string
    targetNickName: string
    targetAvatar: string
  }): Promise<Result<void>> {
    return await request.post('/session/save', form)
  }

  public static async draft(draft: string, sessionId: string) {
    return await request.post('/session/draft', { draft: draft, sessionId: sessionId })
  }

  public static async delDraft(sessionId: string) {
    return await request.post('/session/draft/del', null, { params: { sessionId } })
  }
}
