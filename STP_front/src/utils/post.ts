/**
 * 将帖子的媒体URL字符串（通常由逗号分隔）解析为干净的URL数组列表
 * @param mediaUrls 逗号分隔的媒体URL字符串，例如 "url1,url2"
 * @returns 解析后的URL列表，例如 ["url1", "url2"]，如输入为空则返回空数组
 */
export function parseMediaUrls(mediaUrls: string | null | undefined): string[] {
  if (!mediaUrls) {
    return []
  }
  return mediaUrls
    .split(',')
    .map((url) => url.trim())
    .filter((url) => url.length > 0)
}
