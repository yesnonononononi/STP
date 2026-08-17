import {onMounted, ref, type Ref} from 'vue'
import type {EmojiPackageVO, EmojiVO} from '@/services/entertainment'
import {EntertainmentAPI} from '@/services/entertainment'
import {useAuthStore} from '@/views/auth/store'

// 全局静态缓存，共享接口请求和数据，防止重复请求和重复加载
const globalEmojiTabList = ref<EmojiPackageVO[]>([])
const globalEmojiListMap = ref<Record<string | number, EmojiVO[]>>({})

let emojiTabPromise: Promise<EmojiPackageVO[]> | null = null
const emojiListPromises = new Map<number | string, Promise<EmojiVO[]>>()

async function fetchEmojiPackages(): Promise<EmojiPackageVO[]> {
  const authStore = useAuthStore()
  if (!authStore.token) {
    return []
  }
  if (globalEmojiTabList.value.length > 0) {
    return globalEmojiTabList.value
  }
  if (!emojiTabPromise) {
    emojiTabPromise = EntertainmentAPI.getEmojiPackages()
      .then((res) => {
        const data = res.data || []
        globalEmojiTabList.value = data
        return data
      })
      .finally(() => {
        emojiTabPromise = null
      })
  }
  return emojiTabPromise
}

async function fetchEmojiList(tabId: number | string): Promise<EmojiVO[]> {
  const authStore = useAuthStore()
  if (!authStore.token) {
    return []
  }
  if (globalEmojiListMap.value[tabId]) {
    return globalEmojiListMap.value[tabId]
  }
  let promise = emojiListPromises.get(tabId)
  if (!promise) {
    promise = EntertainmentAPI.getEmojiList(tabId)
      .then((res) => {
        const data = res.data || []
        globalEmojiListMap.value[tabId] = data
        return data
      })
      .finally(() => {
        emojiListPromises.delete(tabId)
      })
    emojiListPromises.set(tabId, promise)
  }
  return promise
}

/**
 * 封装表情选择面板的展示状态、表情列表加载、光标预览控制与表情插入的组合函数
 * @param model 绑定编辑器富文本内容的 Ref 状态
 */
export function useEmoji(model: Ref<string>, textareaRef?: Ref<any>) {
  const emojiList = ref<EmojiVO[]>([])
  const emojiTabList = ref<EmojiPackageVO[]>()

  /**
   * 根据表情索引计算缩放预览提示框的弹出方向
   * @param index 表情在列表中的索引值
   */
  function identifyDirection(index: number): boolean {
    return ((index + (5 - (index % 5))) / 5) % 2 === 0
  }

  /**
   * 往输入框的模型中在光标位置处插入表情文本占位符 (例如 [三花猫])
   * @param emoji 表情实体数据对象
   */
  function insertEmoji(emoji: EmojiVO) {
    const textarea = textareaRef?.value?.$el?.querySelector(
      'textarea',
    ) as HTMLTextAreaElement | null
    
    const text = emoji.name.startsWith('[') && emoji.name.endsWith(']')
      ? emoji.name
      : `[${emoji.name}]`

    if (!textarea) {
      model.value = (model.value || '') + text
      return
    }
    const startPos = textarea.selectionStart
    const endPos = textarea.selectionEnd
    const value = model.value || ''
    model.value = value.substring(0, startPos) + text + value.substring(endPos)

    setTimeout(() => {
      textarea.focus()
      textarea.selectionStart = textarea.selectionEnd = startPos + text.length
    }, 0)
  }

  const activeTabId = ref<number | string | null>(null)

  /**
   * 异步加载表情包分类页签列表
   */
  async function loadEmojiTab() {
    const data = await fetchEmojiPackages()
    emojiTabList.value = data
  }

  /**
   * 切换表情包页签并加载对应表情列表
   */
  async function selectTab(tabId: number | string) {
    activeTabId.value = tabId
    const data = await fetchEmojiList(tabId)
    emojiList.value = data
  }

  onMounted(async () => {
    await loadEmojiTab()
    if (emojiTabList.value && emojiTabList.value.length > 0) {
      await selectTab(emojiTabList.value[0]!.id)
    }
  })

  return {
    emojiList,
    emojiTabList,
    activeTabId,
    selectTab,
    identifyDirection,
    insertEmoji,
  }
}
