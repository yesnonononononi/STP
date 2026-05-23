export class TokenRefreshException extends Error {
  constructor() {
    super('刷新Token异常')
    this.name = 'TokenRefreshException'
  }
}
