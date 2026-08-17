export interface Result<T = any> {
  code: number
  data: T
  errMsg: string | null
}

export interface PageResult<T = any> {
  page: number
  total: number
  data: T
}

export interface CursorPageResult<T> {
  list: T[]
  cursor: string
  hasMore: boolean
}
