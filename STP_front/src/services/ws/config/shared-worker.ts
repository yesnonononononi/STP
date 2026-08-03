import { io, Socket } from 'socket.io-client'
import { socket_opt, type Config, type Type, WsEventName } from './config'

export class Messenger {
  config: Config | undefined
  socket: Socket | undefined
  ports: Set<MessagePort> | undefined
  currentToken: string | undefined
  currentUserId: string | undefined
  currentWsServerUrl: string | undefined

  constructor(config: Config) {
    this.config = config
    this.ports = new Set()
  }
  boardcast(msg: { type: string; data: any }) {
    this.ports?.forEach((port) => {
      port.postMessage(msg)
    })
  }
  connectWithAuth(token: string, userId: string, wsServerUrl?: string, reconnectionAttempts?: number) {
    if (!this.config) throw new Error('config is not defined')

    console.log(
      '【WS-DEBUG】WebSocket 连接尝试建立，执行环境:',
      typeof window === 'undefined' ? '【独立隔离的 SharedWorker 线程 (网络包已对页面隐身)】' : '【主线程 (降级通道 / 开发模式限制，网络包在页面可见)】'
    )

    const wsUrl = wsServerUrl || import.meta.env.VITE_WS_SERVER_URL

    // 如果已经存在连接且鉴权参数及目标服务器地址均未发生改变，则不重复连接
    if (this.socket && this.currentToken === token && this.currentUserId === userId && this.currentWsServerUrl === wsUrl) {
      if (this.socket.connected) {
        return
      }
    }

    // 若存在旧 of Socket，先进行关闭
    if (this.socket) {
      this.socket.close()
    }

    this.currentToken = token
    this.currentUserId = userId
    this.currentWsServerUrl = wsUrl
    if (!wsUrl) {
      console.error('【WS-ERROR】未检测到 VITE_WS_SERVER_URL 环境变量，请确认根目录 .env 文件配置正确（例如等号两边不应有空格），并重启 Vite 开发服务器。')
    }

    this.socket = io(wsUrl, {
      ...socket_opt,
      reconnectionAttempts: reconnectionAttempts !== undefined ? reconnectionAttempts : (socket_opt as any).reconnectionAttempts,
      query: { token, UID: userId },
      transports: ['websocket'],
    } as any)

    this.socket.on('connect', () => this?.config?.onConnect(this))
    this.socket.on('disconnect', () => this?.config?.onDisConnect(this))
    
    // 监听对齐后端的事件
    this.socket.on(WsEventName.NORMAL_MESSAGE, (msg: any) => {
      this.boardcast({ type: WsEventName.NORMAL_MESSAGE, data: msg })
    })
    this.socket.on(WsEventName.WITHDRAWN_MESSAGE, (msg: any) => {
      this.boardcast({ type: WsEventName.WITHDRAWN_MESSAGE, data: msg })
    })
    this.socket.on(WsEventName.READ_MESSAGE, (msg: any) => {
      this.boardcast({ type: WsEventName.READ_MESSAGE, data: msg })
    })
  }
  onconnect = (e: MessageEvent) => {
    const p = e.ports[0]
    this.ports!.add(p!)
    p!.onmessage = (e: MessageEvent) => {
      if (e.data.type === 'auth_init') {
        const { token, userId, wsServerUrl, reconnectionAttempts } = e.data.data
        this.connectWithAuth(token, userId, wsServerUrl, reconnectionAttempts)
        return
      }
      if (e.data.type === 'page_unload') {
        this.ports!.delete(p!)
        if (this.ports!.size === 0 && this.socket) {
          this.socket.close()
          this.socket = undefined
        }
        return
      }
      if (this.socket) {
        this.socket.emit(e.data.type, e.data.data)
      }
    }
    p?.start()
  }
}
export const messenger_config: Config = {
  onConnect: (messenger: Messenger) => {
    if (messenger.socket) {
      messenger.boardcast({ type: 'connect', data: messenger.socket.id })
    }
  },
  onDisConnect: (messenger: Messenger) => {
    if (messenger.socket) {
      messenger.boardcast({ type: 'disconnect', data: messenger.socket.id })
    }
  },
  onMessage: (messenger: Messenger, msg: Type) => {
    messenger.boardcast(msg)
  },
}
export const messenger = new Messenger(messenger_config)
;(self as any).onconnect = (e: any) => messenger.onconnect(e)
