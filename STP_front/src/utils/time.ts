export class TimeUtils {
  /**
   * 统一解析 Date 对象的辅助方法，处理不同格式和浏览器兼容性（特别是 Safari）
   */
  private static parseDate(time: any): Date | null {
    if (!time) return null
    if (time instanceof Date) return time

    const str = String(time).trim()
    if (!str || str === 'null' || str === 'undefined') return null

    // 1. 如果是纯数字字符串（或数字），视为时间戳
    if (/^\d+$/.test(str)) {
      let num = parseInt(str, 10)
      // 如果是 10 位时间戳（秒），转换为 13 位时间戳（毫秒）
      if (str.length === 10) {
        num *= 1000
      }
      return new Date(num)
    }

    // 2. 如果是标准日期时间字符串
    // 针对 Safari 浏览器兼容性：Safari 无法解析带有中划线和空格的格式（如 "2026-05-26 18:15:11"）
    // 如果字符串中不包含时区标志（Z 或 + 或 -开头的时区偏移值），将其替换为斜杠以作为本地时间解析
    let formattedStr = str
    if (!str.includes('Z') && !str.includes('+') && !/-\d{2}:\d{2}$/.test(str)) {
      // 替换 T 为空格，替换 - 为 /
      formattedStr = str.replace(/T/g, ' ').replace(/-/g, '/')
      // 去除毫秒部分 (如 .000)
      formattedStr = formattedStr.split('.')[0] || ''
    } else {
      if (!str.includes('T')) {
        formattedStr = str.replace(/-/g, '/')
      }
    }
    
    const parsedDate = new Date(formattedStr)
    if (isNaN(parsedDate.getTime())) {
      return new Date(str) // 降级直接解析原字符串
    }
    return parsedDate
  }

  /**
   * 计算目标时间到现在的时间差（秒）
   * @param endTime 目标时间
   * @returns 时间差
   */
  static calculateTimeByNow(endTime: string | number | Date | undefined | null): number {
    const date = this.parseDate(endTime)
    if (!date || isNaN(date.getTime())) return 0
    return Math.floor((date.getTime() - Date.now()) / 1000)
  }

  /**
   * 格式化时间戳/时间字符串为可读日期 "YYYY-MM-DD HH:mm:ss"
   */
  static timestampToDate(timestamp: string | number | Date | undefined | null): string {
    const date = this.parseDate(timestamp)
    if (!date || isNaN(date.getTime())) {
      return '未知'
    }

    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    const seconds = String(date.getSeconds()).padStart(2, '0')
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
  }
}
