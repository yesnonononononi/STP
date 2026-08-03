import type { CursorPageResult, Result } from '@/types/result'
import request from '@/services/request'
import type { CommentVO, CreateCommentRequest, CommentReplyQueryRequest } from './types'
import { XssUtils } from '@/utils/xss'

/**
 * 评论管理接口 (CommentAPI)
 */
export class CommentAPI {
  /**
   * 发表/新增评论
   * @param comment 评论信息
   * @returns 操作结果
   */
  static async post(comment: CreateCommentRequest): Promise<Result<CommentVO>> {
    if (comment && comment.content) {
      comment.content = XssUtils.filter(comment.content)
    }
    return await request.post('/post/comment/post', comment)
  }

  /**
   * 删除评论
   * @param id 评论ID
   * @returns 操作结果
   */
  static async delete(id: string): Promise<Result<void>> {
    return await request.delete(`/post/comment/${id}`)
  }

  /**
   * 获取帖子的评论列表
   * @param postId 帖子ID
   * @param cursor 游标（可选）
   * @param limit 每页数量
   * @returns 评论分页结果
   */
  static async getByPostId(form: {
    postId: string
    idCursor: string
    hsCursor: string
    limit: number
  }): Promise<Result<CursorPageResult<CommentVO>>> {
    return await request.post(`/post/comment/post/list`, form)
  }

  /**
   * 获取帖子评论下的回复列表
   * @param query 查询参数
   * @returns 回复分页结果
   */
  static async getReplies(
    query: CommentReplyQueryRequest,
  ): Promise<Result<CursorPageResult<CommentVO>>> {
    return await request.post('/post/comment/search/reply', query)
  }

  /**
   * 点赞评论
   * @param id 评论ID
   * @returns 操作结果
   */
  static async like(id: string): Promise<Result<Boolean>> {
    return await request.get(`/post/comment/like/${id}`)
  }

  static async top(commentId: string, postId: string): Promise<Result<void>> {
    return await request.get(`/post/comment/top/${postId}`, { params: { commentId } })
  }
}
