import {useAuthStore} from '@/views/auth/store'
import {useUserInfoStore} from '@/stores/userInfo'
import type {Operation} from './config'
import {WsEventName} from './config'
import {EnvType, EnvUtils} from '@/utils/env'

export class Connector {
  private worker: SharedWorker | undefined
  private localPort: MessagePort | undefined
  private isWorkerMode: boolean = false
  started: boolean = false

  private static instance: Connector | null = null

  public static getInstance(): Connector {
    if (!Connector.instance) {
      Connector.instance = new Connector()
    }
    return Connector.instance
  }

  constructor() {
    const env = EnvUtils.getEnvType()
    if (env === EnvType.SHARED_WORKER) {
      try {
        // 使用标准的 SharedWorker 实例化方式，配合 Vite 的 URL 机制，自动打包为 Module SharedWorker
        this.worker = new SharedWorker(
          new URL('./shared-worker.ts', import.meta.url),
          {
            type: 'module',
            name: 'STP-Shared-Worker'
          }
        )
        this.isWorkerMode = true
      } catch (e) {
        console.warn('【WebSocket】初始化 SharedWorker 失败，启用降级本地通道:', e)
        this.initFallback()
      }
    } else {
      this.initFallback()
    }

    // 监听页面卸载，主动告知后台清理端口，防范内存泄漏
    if (typeof window !== 'undefined') {
      window.addEventListener('beforeunload', () => {
        const port = this.isWorkerMode ? this.worker?.port : this.localPort
        if (port) {
          port.postMessage({ type: 'page_unload' })
        }
      })
    }
  }

  private async initFallback() {
    this.isWorkerMode = false
    const channel = new MessageChannel()
    this.localPort = channel.port2

    // 动态载入，确保在不需要降级的主流环境中，主 Bundle 不会打包 WebSocket 的核心连接库
    const { messenger } = await import('./shared-worker')

    messenger.onconnect({
      ports: [channel.port1],
    } as unknown as MessageEvent)

    // 如果在降级模式下且启动监听已触发，需补发 auth 消息完成连接建立
    if (this.started && this.localPort) {
      this.sendAuthMessage(this.localPort)
    }
  }

  public getWorker(): SharedWorker | undefined {
    return this.worker
  }

  private sendAuthMessage(port: MessagePort) {
    const authStore = useAuthStore()
    const userInfoStore = useUserInfoStore()
    const wsUrl = import.meta.env.VITE_WS_SERVER_URL
    if (!wsUrl) {
      console.error('【WebSocket-Connector】未检测到 VITE_WS_SERVER_URL，将降级连接到当前 Page Origin，请确认根目录 .env 配置并重启 Vite 开发服务器。')
    }
    port.postMessage({
      type: 'auth_init',
      data: {
        token: authStore.token,
        userId: userInfoStore.userId,
        wsServerUrl: wsUrl,
        reconnectionAttempts: import.meta.env.VITE_WS_RECONNECTION_ATTEMPTS ? Number(import.meta.env.VITE_WS_RECONNECTION_ATTEMPTS) : undefined,
      },
    })
  }

  public startListener(operation: Operation) {
    try {
      const port = this.isWorkerMode ? this.worker?.port : this.localPort

      // 如果处于降级模式但动态模块尚未加载完（localPort 为空），此时依然先标记 started=true，待加载完成后补发
      if (port) {
        this.sendAuthMessage(port)
      }

      if (this.started) return

      const setupListener = (activePort: MessagePort) => {
        activePort.addEventListener('message', (e: MessageEvent) => {
          const type = e.data.type
          switch (type) {
            case 'connect':
              operation.onConnect(e.data.data)
              break
            case 'disconnect':
              operation.onDisConnect(e.data.data)
              break
            case 'message':
            case WsEventName.NORMAL_MESSAGE:
            case WsEventName.WITHDRAWN_MESSAGE:
            case WsEventName.READ_MESSAGE:
              operation.onMessage(e.data)
              break
            default:
              console.log('unknown message type: ', type)
              break
          }
        })
        activePort.start()
      }

      if (this.isWorkerMode && this.worker) {
        setupListener(this.worker.port)
      } else {
        // 降级模式：若 localPort 已就绪则立刻建立监听，若未就绪（异步加载中）则借助 setInterval 等待其就绪并绑定
        if (this.localPort) {
          setupListener(this.localPort)
        } else {
          const timer = setInterval(() => {
            if (this.localPort) {
              setupListener(this.localPort)
              clearInterval(timer)
            }
          }, 50)
        }
      }

      this.started = true
    } catch (e) {
      console.error('【WebSocket】通信连接建立失败:', e)
      throw e
    }
  }
}
