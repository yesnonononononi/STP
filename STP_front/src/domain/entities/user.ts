export interface User {
  id: string
  username: string
  email: string
  phone: string
  avatar: string
  createTime: string
  updateTime: string
  status: number
}

export interface UserItem {
  id: string
  avatar: string
  nick: string
  level: number
  ip: string
  liked?: string
  topic?: string
  fans?: string
  isVip?: number
  endTime?: string
}
