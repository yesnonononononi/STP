import type { Result } from '@/types/result'
import { log } from './log'
class Check {
  check(res: Result<any>) {
    if (!res || res.code !== 1) {
      log.error(res.errMsg || '未知错误')
    }
  }
}
