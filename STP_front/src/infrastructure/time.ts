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
}
