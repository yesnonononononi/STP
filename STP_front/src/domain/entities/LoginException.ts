export class LoginException extends Error {
  constructor(message: string = '登录异常') {
    super(message)
    this.name = 'LoginException'
  }
}
