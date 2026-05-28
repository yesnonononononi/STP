import type { Result } from '@/types/result'
import request from '@/services/request'
import type { PostPO, PostVO, TagPO, PostTagRelPO, PageResult, CreatePostRequest, UpdatePostRequest, CreateTagRequest, UpdateTagRequest, TagVO } from './types'

/**
 * 帖子管理接口 (PostAPI)
 */
export class PostAPI {
  /**
   * 根据帖子ID获取帖子详情 (已升级为 VO)
   */
  static async getById(id: number): Promise<Result<PostVO>> {
    return await request.get(`/post/${id}`)
  }

  /**
   * 分页获取帖子列表 (对接后端游标分页VO接口)
   */
  static async getPage(query: {
    cursor: string
    self: boolean
    creatorId?: string | number
    status?: number
  }): Promise<Result<PostVO[]>> {
    return await request.post('/post/page', query)
  }

  /**
   * 发布新帖子
   */
  static async create(post: CreatePostRequest): Promise<Result<void>> {
    return await request.post('/post/create', post)
  }

  /**
   * 编辑/更新帖子
   */
  static async update(post: UpdatePostRequest): Promise<Result<void>> {
    return await request.put('/post/update', post)
  }

  /**
   * 删除帖子
   */
  static async delete(id: number): Promise<Result<void>> {
    return await request.delete(`/post/${id}`)
  }

  /**
   * 点赞/取消点赞指定帖子
   */
  static async like(id: number): Promise<Result<void>> {
    return await request.post(`/post/like/${id}`)
  }

  /**
   * 收藏/取消收藏指定帖子
   */
  static async collect(id: number): Promise<Result<void>> {
    return await request.post(`/post/collect/${id}`)
  }

  /**
   * 获取当前登录用户对指定帖子的点赞状态
   */
  static async getLikeStatus(id: number): Promise<Result<boolean>> {
    return await request.get(`/post/like/status/${id}`)
  }

  /**
   * 获取当前登录用户对指定帖子的收藏状态
   */
  static async getCollectStatus(id: number): Promise<Result<boolean>> {
    return await request.get(`/post/collect/status/${id}`)
  }
}

/**
 * 标签管理接口 (TagAPI)
 */
export class TagAPI {
  /**
   * 根据标签ID获取标签详情
   */
  static async getById(id: number): Promise<Result<TagVO>> {
    return await request.get(`/post/tag/${id}`)
  }

  /**
   * 创建新标签
   */
  static async create(tag: CreateTagRequest): Promise<Result<void>> {
    return await request.post('/post/tag/create', tag)
  }

  /**
   * 编辑/更新标签
   */
  static async update(tag: UpdateTagRequest): Promise<Result<void>> {
    return await request.put('/post/tag/update', tag)
  }

  /**
   * 删除标签
   */
  static async delete(id: number): Promise<Result<void>> {
    return await request.delete(`/post/tag/${id}`)
  }

  /**
   * 分页获取标签列表
   */
  static async getPage(page: number, pageSize: number): Promise<Result<PageResult<TagVO>>> {
    return await request.get('/post/tag/page', {
      params: { page, pageSize }
    })
  }
}

/**
 * 帖子标签关联管理接口 (PostTagRelAPI)
 */
export class PostTagRelAPI {
  /**
   * 绑定标签到帖子
   */
  static async bind(postId: number, tagId: number): Promise<Result<void>> {
    return await request.post('/post/relation/bind', null, {
      params: { postId, tagId }
    })
  }

  /**
   * 解绑帖子标签
   */
  static async unbind(id: number): Promise<Result<void>> {
    return await request.delete(`/post/relation/unbind/${id}`)
  }

  /**
   * 清空帖子的所有标签
   */
  static async clear(postId: number): Promise<Result<void>> {
    return await request.delete(`/post/relation/clear/${postId}`)
  }

  /**
   * 获取帖子关联的全部关系
   */
  static async getRelationsByPostId(postId: number): Promise<Result<PostTagRelPO[]>> {
    return await request.get(`/post/relation/post/${postId}`)
  }

  /**
   * 获取标签关联的全部关系
   */
  static async getRelationsByTagId(tagId: number): Promise<Result<PostTagRelPO[]>> {
    return await request.get(`/post/relation/tag/${tagId}`)
  }
}
