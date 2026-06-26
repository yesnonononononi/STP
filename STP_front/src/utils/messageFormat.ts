import type { messageVO } from '@/services/message/message'
import { parseEmoji } from '@/utils/emoji'

/**
 * 适配并填充 messageVO 中的 user 对象，以防前端因缺少 user 属性报空指针异常。
 * 
 * @param msg 原始消息数据
 * @param me 当前用户对象
 * @param friend 当前聊天好友对象
 */
export function formatMessage(msg: any, me: any, friend: any): messageVO {
  if (msg && !msg.user) {
    const isMe = String(msg.userId) === String(me?.id)
    msg.user = isMe ? me : friend?.user
  }
  if (msg && msg.content) {
    msg.content = parseEmoji(msg.content)
  }
  return msg as messageVO
}
