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
  if (!content) return ''
  if (!tags || tags.length === 0) {
    return content
  }
  let result = content
  tags.forEach(tag => {
    const tagName = tag.tagName || tag.name || ''
    if (!tagName) return
    const escapedTagName = tagName.replace(/[-\/\\^$*+?.()|[\]{}]/g, '\\$&')
    // 优先匹配被双井号包围的格式：#标签名#
    const regexWithTwoHashes = new RegExp(`#${escapedTagName}#`, 'g')
    result = result.replace(regexWithTwoHashes, `<a href="#" onclick="event.preventDefault();" style="color: #60a5fa; cursor: pointer; text-decoration: none;">#${tagName}#</a>`)
    // 兼容匹配单井号格式：#标签名 (后面没有#)
    const regexWithOneHash = new RegExp(`#${escapedTagName}(?!#)`, 'g')
    result = result.replace(regexWithOneHash, `<a href="#" onclick="event.preventDefault();" style="color: #60a5fa; cursor: pointer; text-decoration: none;">#${tagName}</a>`)
  })
  return result
}
