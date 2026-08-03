import request from '@/services/request'
import type { Result } from '@/types/result'
import type {
  CreatorRankVO,
  PostRankVO,
  TopicRankVO,
  MappedCommonRank,
  MappedCreatorRank,
} from './types'

export class RankBoardAPI {
  static async getRankListByTabId(tabId: 'creator', size?: number): Promise<MappedCreatorRank[]>
  static async getRankListByTabId(
    tabId: 'post' | 'topic',
    size?: number,
  ): Promise<MappedCommonRank[]>
  static async getRankListByTabId(
    tabId: string,
    size?: number,
  ): Promise<(MappedCommonRank | MappedCreatorRank)[]>
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
        return res.data.map((item) => {
          let decoration = ''
          if (item.rank === 1) {
            decoration =
              'http://localhost:9001/api/v1/download-shared-object/aHR0cDovLzEyNy4wLjAuMTo5MDAwL3N0cC1zdW1taXQtZmlsZXMvYXZhdGFyL2RlY29yYXRpb24vMjAyNTAxMDIxNzM1ODA1NjYzODc1ODQ3NC5wbmc_WC1BbXotQWxnb3JpdGhtPUFXUzQtSE1BQy1TSEEyNTYmWC1BbXotQ3JlZGVudGlhbD1BUThUT1JDRFk3RkxOVUFVQjVBWiUyRjIwMjYwNzAxJTJGdXMtZWFzdC0xJTJGczMlMkZhd3M0X3JlcXVlc3QmWC1BbXotRGF0ZT0yMDI2MDcwMVQxMzA5MDFaJlgtQW16LUV4cGlyZXM9NDMyMDAmWC1BbXotU2VjdXJpdHktVG9rZW49ZXlKaGJHY2lPaUpJVXpVeE1pSXNJblI1Y0NJNklrcFhWQ0o5LmV5SmhZMk5sYzNOTFpYa2lPaUpCVVRoVVQxSkRSRmszUmt4T1ZVRlZRalZCV2lJc0ltVjRjQ0k2TVRjNE1qazFOREl6T0N3aWNHRnlaVzUwSWpvaVlXUnRhVzRpZlEuNlVZOElwX1l1aGM5MjhndDY1bGtKRGRoX2ZfdUI0ck5BM3BrUEwzbVhONUhmbTU0SHZRenoxbDBIb0xFMWRWYXE1V29WZnpjeVc4Mjh5UG1jamU1UUEmWC1BbXotU2lnbmVkSGVhZGVycz1ob3N0JnZlcnNpb25JZD1udWxsJlgtQW16LVNpZ25hdHVyZT1lODA1NzYwZWU0OWYxNmMwZDRlZTU3ZTZmYTkxNjRiOTM2OTk0NmU0ZmY0OTQ5NDc3MmJmNzQyNjg2MzkzNWJk'
          } else if (item.rank === 2) {
            decoration =
              'http://localhost:9001/api/v1/download-shared-object/aHR0cDovLzEyNy4wLjAuMTo5MDAwL3N0cC1zdW1taXQtZmlsZXMvYXZhdGFyL2RlY29yYXRpb24vMjAyNTAxMDIxNzM1ODA1Njc5MTE0NDE3NS5wbmc_WC1BbXotQWxnb3JpdGhtPUFXUzQtSE1BQy1TSEEyNTYmWC1BbXotQ3JlZGVudGlhbD1BUThUT1JDRFk3RkxOVUFVQjVBWiUyRjIwMjYwNzAxJTJGdXMtZWFzdC0xJTJGczMlMkZhd3M0X3JlcXVlc3QmWC1BbXotRGF0ZT0yMDI2MDcwMVQxMzA1MzRaJlgtQW16LUV4cGlyZXM9NDMyMDAmWC1BbXotU2VjdXJpdHktVG9rZW49ZXlKaGJHY2lPaUpJVXpVeE1pSXNJblI1Y0NJNklrcFhWQ0o5LmV5SmhZMk5sYzNOTFpYa2lPaUpCVVRoVVQxSkRSRmszUmt4T1ZVRlZRalZCV2lJc0ltVjRjQ0k2TVRjNE1qazFOREl6T0N3aWNHRnlaVzUwSWpvaVlXUnRhVzRpZlEuNlVZOElwX1l1aGM5MjhndDY1bGtKRGRoX2ZfdUI0ck5BM3BrUEwzbVhONUhmbTU0SHZRenoxbDBIb0xFMWRWYXE1V29WZnpjeVc4Mjh5UG1jamU1UUEmWC1BbXotU2lnbmVkSGVhZGVycz1ob3N0JnZlcnNpb25JZD1udWxsJlgtQW16LVNpZ25hdHVyZT1mOWI0MTU4MmVmMzc4NjcyYjQxZDcyODM4YWQ1ZTY4NzNiNGYyNWQ5ZjgyNjEzOWQ3N2I4NTMyN2FhOTE3ZTIw'
          } else if (item.rank === 3) {
            decoration =
              'http://localhost:9001/api/v1/download-shared-object/aHR0cDovLzEyNy4wLjAuMTo5MDAwL3N0cC1zdW1taXQtZmlsZXMvYXZhdGFyL2RlY29yYXRpb24vMjAyNTAxMDIxNzM1ODA1Njg4NzU4MjExOS5wbmc_WC1BbXotQWxnb3JpdGhtPUFXUzQtSE1BQy1TSEEyNTYmWC1BbXotQ3JlZGVudGlhbD1BUThUT1JDRFk3RkxOVUFVQjVBWiUyRjIwMjYwNzAxJTJGdXMtZWFzdC0xJTJGczMlMkZhd3M0X3JlcXVlc3QmWC1BbXotRGF0ZT0yMDI2MDcwMVQxNTIyMTRaJlgtQW16LUV4cGlyZXM9NDMyMDAmWC1BbXotU2VjdXJpdHktVG9rZW49ZXlKaGJHY2lPaUpJVXpVeE1pSXNJblI1Y0NJNklrcFhWQ0o5LmV5SmhZMk5sYzNOTFpYa2lPaUpCVVRoVVQxSkRSRmszUmt4T1ZVRlZRalZCV2lJc0ltVjRjQ0k2TVRjNE1qazFOREl6T0N3aWNHRnlaVzUwSWpvaVlXUnRhVzRpZlEuNlVZOElwX1l1aGM5MjhndDY1bGtKRGRoX2ZfdUI0ck5BM3BrUEwzbVhONUhmbTU0SHZRenoxbDBIb0xFMWRWYXE1V29WZnpjeVc4Mjh5UG1jamU1UUEmWC1BbXotU2lnbmVkSGVhZGVycz1ob3N0JnZlcnNpb25JZD1udWxsJlgtQW16LVNpZ25hdHVyZT0zY2M1ZjMwZWQwOTUzZDZkMDFiYzFjMzI5YzU4MDljMzNiMmNlODBhNTg3YjQyYTg0MTNiNDRhMGY3MmYyOTY3'
          }
          return {
            id: item.entityId,
            sort: item.rank,
            score: item.score,
            nickname: item.entityInfo?.nick || '',
            avatar: item.entityInfo?.avatar || '',
            decoration: decoration,
            ip: item.entityInfo?.ip || '未知地区',
          }
        })
      }
    }
    return []
  }
}
