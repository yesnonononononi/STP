import { ref } from 'vue'
import { Connector } from '../config/connector'
import { useAuthStore } from '@/views/auth/store'

export const IM = 'im-message'
export const start = () => {
  if (!useAuthStore().token) return
  const connector = Connector.getInstance()
  connector.startListener({
    onConnect: (id) => {
      console.log('connect', id)
    },
    onDisConnect: (id) => {
      console.log('disconnect', id)
    },
    onMessage: (msg) => {
      window.dispatchEvent(new CustomEvent(IM, { detail: msg }))
    },
  })
  window.addEventListener(IM, handleIMMessage)
}
export function removeIMListener() {
  window.removeEventListener(IM, handleIMMessage)
}
function handleIMMessage(e: Event) {
  const customEvent = e as CustomEvent
  const { type, data } = customEvent.detail
}
