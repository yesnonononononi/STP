export interface Result<T = any> {
  code: number
  data: T
  errMsg: string | null
}
