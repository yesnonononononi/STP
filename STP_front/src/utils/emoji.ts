import { ref } from 'vue'
import { EntertainmentAPI } from '@/services/entertainment'
import { useAuthStore } from '@/views/auth/store'

// 全局表情 HTML 映射缓存：emojiName -> emojiHtmlString (注：emojiName 已包含中括号，如 "[可达鸭]")
export const emojiMap = ref<Record<string, string>>({})
let isLoaded = false

/**
 * 异步初始化表情缓存，遍历所有表情包加载表情列表
 */
export async function loadEmojiCache() {
  const authStore = useAuthStore()
  if (!authStore.token) return

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
  return content.replace(/\[([^\]]+)\]/g, (match) => {
    // 这里的 match 包含中括号，例如 "[可达鸭]"
    const emojiHtml = emojiMap.value[match]
    if (emojiHtml) {
      return emojiHtml
    }
    return match
  })
}
