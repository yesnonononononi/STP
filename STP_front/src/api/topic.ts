import type { Result } from '@/shared/types/result'

export interface TopicItem {
  id: string
  publisher: {
    id: string
    avatar: string
    nick: string
    level: number
    ip: string
    fans?: number
    topic?: number
    liked?: number
  }
  content: {
    type: string
    title: string
    content: string
    img: string
    video: string
    audio: string
  }
  publishTime: string
  likeCount: string
  replyCount: string
  isLiked: boolean
  collectedCount: string
  isCollected: boolean
}

export interface TopicDetail {
  id: string
}

export interface TabSymbol {
  id: string
  name: string
  description?: string
}

export class Topic {
  queryTopicList(symbolId: string): Result<TopicItem[]> {
    return {
      code: 1,
      data: [
        {
          id: '1',
          publisher: {
            id: '1',
            avatar: '',
            nick: '张三sfafdsdfsdfsdfsdf',
            level: 1,
            ip: '湖南',
          },
          content: {
            type: 'text',
            title: '这是标题',
            content: '这是评论内容',
            img: '',
            video: '',
            audio: '',
          },
          publishTime: '5-16 09:05:05',
          likeCount: '12',
          replyCount: '1',
          isLiked: true,
          collectedCount: '',
          isCollected: true,
        },
      ],
      errMsg: null,
    }
  }
  queryTopicById(topicId: string): Result<TopicDetail> {
    throw new Error('Method not implemented.')
  }
  like(topicId: string): Result<boolean> {
    throw new Error('Method not implemented.')
  }
  collect(topicId: string): Result<boolean> {
    throw new Error('Method not implemented.')
  }
}

export class TopicTab {
  queryTabListByUId(uid: string): Result<TabSymbol[]> {
    throw new Error('Method not implemented.')
  }
  saveTab(symbolId: string): Result<boolean> {
    throw new Error('Method not implemented.')
  }
  deleteTab(symbolId: string): Result<boolean> {
    throw new Error('Method not implemented.')
  }
}
