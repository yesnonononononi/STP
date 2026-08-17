import type {ManagerOptions, SocketOptions} from 'socket.io-client'

export const server_location = import.meta.env.VITE_WS_SERVER_LOCATION
export interface Type {
  type: string
  data: any
}
export enum WsEventName {
  NORMAL_MESSAGE = 'normal_message',
  WITHDRAWN_MESSAGE = 'withdrawn_message',
  READ_MESSAGE = 'read_message',
}
export interface Config {
  onConnect: (messenger: any) => void
  onDisConnect: (messenger: any) => void
  onMessage: (messenger: any, msg: Type) => void
}
export interface Operation {
  onConnect: (id: any) => void
  onDisConnect: (id: any) => void
  onMessage: (msg: Type) => void
}
export const socket_opt: SocketOptions | ManagerOptions = {
  ackTimeout: import.meta.env.VITE_WS_ACK_TIMEOUT ? Number(import.meta.env.VITE_WS_ACK_TIMEOUT) : undefined,
  retries: import.meta.env.VITE_WS_RETRIES ? Number(import.meta.env.VITE_WS_RETRIES) : undefined,
  timeout: import.meta.env.VITE_WS_TIMEOUT ? Number(import.meta.env.VITE_WS_TIMEOUT) : undefined,
  reconnection: true,
  reconnectionAttempts: import.meta.env.VITE_WS_RECONNECTION_ATTEMPTS ? Number(import.meta.env.VITE_WS_RECONNECTION_ATTEMPTS) : Infinity,
  reconnectionDelay: 1000,
  reconnectionDelayMax: 5000,
}
