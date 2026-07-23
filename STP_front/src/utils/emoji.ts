import { ref } from 'vue'
import { EntertainmentAPI } from '@/services/entertainment'

// 全局表情 HTML 映射缓存：emojiName -> emojiHtmlString (注：emojiName 已包含中括号，如 "[可达鸭]")
export const emojiMap = ref<Record<string, string>>({})
let isLoaded = false

/**
 * 异步初始化表情缓存，遍历所有表情包加载表情列表
 */
export async function loadEmojiCache() {
  if (isLoaded) return
  isLoaded = true
  try {
    const packagesRes = await EntertainmentAPI.getEmojiPackages()
    if (packagesRes.code === 1 && packagesRes.data) {
      for (const pkg of packagesRes.data) {
        const listRes = await EntertainmentAPI.getEmojiList(pkg.id)
        if (listRes.code === 1 && listRes.data) {
          listRes.data.forEach(emoji => {
            // 后端返回的 url 即为完整的 img 标签内容
            emojiMap.value[emoji.name] = emoji.url
          })
        }
      }
    }
  } catch (e) {
    console.error('加载表情缓存失败:', e)
    isLoaded = false
  }
}

/**
 * 解析文本中的占位符，例如将 [可达鸭] 替换为相应的 <img> 标签
 */
export function parseEmoji(content: string): string {
  if (!content) return ''
  return content.replace(/\[([^\]]+)\]/g, (match, name) => {
    // 检查 emojiMap 中是否有 "[可达鸭]" 或者是 "可达鸭"
    const emojiHtml = emojiMap.value[match] || emojiMap.value[name]
    if (emojiHtml) {
      // 如果已是完整的 img 标签，直接返回
      if (emojiHtml.startsWith('<img')) {
        return emojiHtml
      }
      // 如果仅是 URL 路径，则自动包装为符合样式标准的 img 标签
      return `<img src="${emojiHtml}" class="emoji" alt="${name}" style="width: 20px; height: 20px; display: inline-block; vertical-align: middle;" />`
    }
    return match
  })
}
