import type {UserSimpleData} from '@/services/user'

/**
 * 帖子内容类型枚举
 */
export enum PostType {
  TEXT = 1,
  IMAGE = 2,
  VIDEO = 3,
  AUDIO = 4,
  LINK = 5,
  FILE = 6,
}

/**
 * 帖子类型展示名称字典
 */
export const PostTypeNames = {
  [PostType.TEXT]: '文字',
  [PostType.IMAGE]: '图片',
  [PostType.VIDEO]: '视频',
  [PostType.AUDIO]: '音频',
  [PostType.LINK]: '链接',
  [PostType.FILE]: '文件',
} as const

/**
 * 帖子状态枚举
 */
export enum PostStatus {
  NORMAL = 1,
  DELETED = 2,
  BLOCKED = 3,
  REPORTED = 4,
  UNKNOWN = 5,
  DRAFT = 6,
  CHECK = 7,   
  UNPASS = 8, 
  COLLECTED = 9,
  LIKED = 10,
}

/**
 * 帖子状态展示名称字典
 */
export const PostStatusNames = {
  [PostStatus.NORMAL]: '正常',
  [PostStatus.DELETED]: '删除',
  [PostStatus.BLOCKED]: '封禁',
  [PostStatus.REPORTED]: '举报',
  [PostStatus.UNKNOWN]: '未知',
  [PostStatus.DRAFT]: '草稿',
  [PostStatus.CHECK]: '待审核',
  [PostStatus.UNPASS]: '审核未通过',
} as const

/**
 * 帖子持久化实体 (与后端PO对应)
 */
export interface PostPO {
  id?: number
  creatorId: number
  title: string
  type: PostType
  content?: string
  mediaUrls?: string
  likeCount?: number
  replyCount?: number
  collectCount?: number
  status?: PostStatus
  createTime?: string
  updateTime?: string
  isTop?: number
  viewCount?: number
}

/**
 * 帖子关联的图片视图对象
 */
export interface PostImageVO {
  id?: string
  postId: string
  imageUrl: string
  width?: number | null
  height?: number | null
  size?: number | null
  sortOrder?: number
  status?: PostStatus | null
  createTime?: number | string | null
}

/**
 * 帖子展示视图对象 (与后端VO对应)
 */
export interface PostVO {
  id: string
  creatorId: string
  title: string
  type: PostType
  content?: string
  mediaUrls?: PostImageVO[] | null
  extraMediaUrl?: string | null
  likeCount?: number
  replyCount?: number
  collectCount?: number
  status?: PostStatus | string | number
  createTime?: number | string
  updateTime?: number | string
  publisher?: UserSimpleData // 发布者详细信息
  tags?: TagVO[] // 帖子关联的标签详情
  isLike?: boolean // 当前用户是否已点赞
  isCollect?: boolean // 当前用户是否已收藏
  isTop?: number
  viewCount?: number
  visibleScope: number
}

/**
 * 分页数据承载实体
 */
export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
}

/**
 * 标签实体
 */
export interface TagPO {
  id: number
  tagName: string
  sort?: number
  useCount?: number
  status?: number
  createTime?: string
}

/**
 * 标签展示实体 (VO)
 */
export interface TagVO {
  id: string | number
  tagName: string
  sort: number
  useCount: number
  status: number
  createTime: number | string
}

/**
 * 创建标签请求参数
 */
export interface CreateTagRequest {
  tagName: string
}

/**
 * 更新标签请求参数
 */
export interface UpdateTagRequest {
  id: string
  tagName?: string
  sort?: number
  status?: number
}

/**
 * 帖子标签关联实体
 */
export interface PostTagRelPO {
  id?: number
  postId: number
  tagId: number
  createTime?: string
}

export interface ImageInfo {
  url: string
  width?: number | null
  height?: number | null
}

/**
 * 创建新帖子请求实体
 */
export interface CreatePostRequest {
  title: string
  type: PostType
  tagIds?: string[]
  content?: string
  mediaUrls?: ImageInfo[]
  status: PostStatus
  isTop?: number
  visibleScope?: number
}

/**
 * 更新帖子请求实体
 */
export interface UpdatePostRequest {
  id: number
  title?: string
  type?: PostType
  content?: string
  tagIds?: string[]
  mediaUrls?: ImageInfo[]
  status?: PostStatus
  isTop?: number
  visibleScope?: number
}

export interface TopicTag {
  id: string | number
  keyword: string
  extra: number
}

export interface suggestion {
  limit: number
  total: number
  keyword: string
  suggestList: TopicTag[]
}
