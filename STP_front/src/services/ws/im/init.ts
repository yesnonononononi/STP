import { Connector } from '../config/connector'
export const start = () => {
  const connector = Connector.getInstance()
  connector.startListener({
    onConnect: (id) => {
      console.log('connect', id)
    },
    onDisConnect: (id) => {
      console.log('disconnect', id)
    },
    onMessage: (msg) => {
      console.log('message', msg)
      window.dispatchEvent(new CustomEvent('im-message', { detail: msg }))
    },
  })
}
