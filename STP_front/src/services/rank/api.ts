import request from '@/services/request'
import type { Result } from '@/types/result'
import type { CreatorRankVO, PostRankVO, TopicRankVO, MappedCommonRank, MappedCreatorRank } from './types'

export class RankBoardAPI {
  static async getRankListByTabId(tabId: 'creator', size?: number): Promise<MappedCreatorRank[]>
  static async getRankListByTabId(tabId: 'post' | 'topic', size?: number): Promise<MappedCommonRank[]>
  static async getRankListByTabId(tabId: string, size?: number): Promise<(MappedCommonRank | MappedCreatorRank)[]>
  /**
   * 根据 TabID 获取排行榜列表并映射为展示组件所需格式
   */
  static async getRankListByTabId(tabId: string, size: number = 10): Promise<any[]> {
    if (tabId === 'post') {
      const res: Result<PostRankVO[]> = await request.get('/rank/hot-search/top-hot', {
        params: { size },
      })
      if (res && res.data) {
        return res.data.map((item) => ({
          id: item.entityId,
          sort: item.rank,
          title: item.entityInfo?.title || item.entityName || '',
          count: item.score,
        }))
      }
    } else if (tabId === 'topic') {
      const res: Result<TopicRankVO[]> = await request.get('/rank/content/subject/hot-subject', {
        params: { size },
      })
      if (res && res.data) {
        return res.data.map((item) => ({
          id: item.entityId,
          sort: item.rank,
          title: item.entityInfo?.tagName || item.entityName || '',
          count: item.score,
        }))
      }
    } else if (tabId === 'creator') {
      const res: Result<CreatorRankVO[]> = await request.get('/rank/content/creator/top-list', {
        params: { size },
      })
      if (res && res.data) {
        return res.data.map((item) => ({
          id: item.entityId,
          sort: item.rank,
          score: item.score,
          nickname: item.entityInfo?.nick || '',
          avatar: item.entityInfo?.avatar || '',
          decoration: item.entityInfo?.vipConfigIcon || '',
          ip: item.entityInfo?.ip || '未知属地',
        }))
      }
    }
    return []
  }
}
