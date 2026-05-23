export class TimeUtils {
  /**
   * 计算目标时间到现在的时间差
   * @param endTime 目标时间
   * @returns 时间差
   */
  static calculateTimeByNow(endTime: string | undefined): number {
    if (!endTime) return 0
    return Math.floor((new Date(endTime).getTime() - new Date().getTime()) / 1000)
  }

  static timestampToDate(timestamp: string | undefined | null): string {
    if (!timestamp) {
      return '未知'
    }
    const str = String(timestamp).trim()
    if (!str || str === 'null' || str === 'undefined') {
      return '未知'
    }
    try {
      let date: Date
      if (/^\d+$/.test(str)) {
        date = new Date(parseInt(str, 10))
      } else {
        date = new Date(str)
      }
      if (isNaN(date.getTime())) {
        return '未知'
      }
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      const seconds = String(date.getSeconds()).padStart(2, '0')
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
    } catch (e) {
      console.error(e)
      return '未知'
    }
  }
}
