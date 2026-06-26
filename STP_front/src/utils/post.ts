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

/**
 * 组装帖子的 tag 标签，将其转化为淡蓝色且可点击的 HTML 超链接格式，并拼接在内容末尾
 * @param content 原始的帖子正文内容
 * @param tags 标签数组，例如 [{ tagName: '开心' }]
 */
export function parseTag(content: string, tags?: any[]): string {
  if (!content) content = ''
  if (!tags || tags.length === 0) {
    return content
  }
  const tagHtmlList = tags.map(tag => {
    const tagName = tag.tagName || tag.name || ''
    if (!tagName) return ''
    return `<a href="#" onclick="event.preventDefault();" style="color: #60a5fa; cursor: pointer; text-decoration: none; margin-right: 8px;">#${tagName}</a>`
  }).filter(Boolean)

  if (tagHtmlList.length === 0) {
    return content
  }
  return `${content} ${tagHtmlList.join('')}`
}
