import dayjs from 'dayjs'

export class TimeUtils {
  /**
   * 安全的 dayjs 转换辅助方法
   */
  private static safeDayjs(time: any): dayjs.Dayjs {
    if (!time) return dayjs()
    if (typeof time === 'string' && /^\d+$/.test(time)) {
      return dayjs(Number(time))
    }
    return dayjs(time)
  }

  /**
   * 获取当前时间戳的字符串形式
   */
  static now(): string {
    return String(Date.now())
  }

  /**
   * 统一解析时间的辅助方法，返回 Date 对象
   */
  public static parseDate(time: any): Date | null {
    if (!time) return null
    const d = TimeUtils.safeDayjs(time)
    return d.isValid() ? d.toDate() : null
  }

  /**
   * 计算目标时间到现在的时间差（秒）
   */
  static calculateTimeByNow(
    endTime: string | number | Date | undefined | null,
    startTime?: number | string,
  ): number {
    if (!endTime) return 0
    const end = TimeUtils.safeDayjs(endTime)
    const start = startTime ? TimeUtils.safeDayjs(startTime) : dayjs()
    if (!end.isValid() || !start.isValid()) return 0
    return Math.floor(end.diff(start, 'second'))
  }

  /**
   * 格式化时间并进行美化：
   * - 今天：HH:mm
   * - 昨天：昨天 HH:mm
   * - 本周内（排除今天 and 昨天）：星期几 HH:mm
   * - 其他：YYYY-MM-DD HH:mm:ss
   */
  static timestampToDate(timestamp: string | number | Date | undefined | null): string {
    if (!timestamp) return '未知'
    const target = TimeUtils.safeDayjs(timestamp)
    if (!target.isValid()) return '未知'

    const now = dayjs()
    const today = now.startOf('day')
    const yesterday = today.subtract(1, 'day')

    // 本周一 00:00:00
    const dayOfWeek = now.day()
    const distanceToMonday = dayOfWeek === 0 ? 6 : dayOfWeek - 1
    const thisWeekStart = today.subtract(distanceToMonday, 'day')

    if (target.isSame(now, 'day')) {
      return target.format('HH:mm')
    } else if (target.isSame(yesterday, 'day')) {
      return `昨天 ${target.format('HH:mm')}`
    } else if (target.isAfter(thisWeekStart) || target.isSame(thisWeekStart, 'day')) {
      const weekDays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
      const weekDayStr = weekDays[target.day()]
      return `${weekDayStr} ${target.format('HH:mm')}`
    } else {
      return target.format('YYYY-MM-DD HH:mm:ss')
    }
  }
}

