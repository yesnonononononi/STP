export interface Auth {
  token: string
  refreshToken: string
  username: string
  expireTime?: number
}
