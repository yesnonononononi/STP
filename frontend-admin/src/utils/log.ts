import {ElMessage} from 'element-plus'
import 'element-plus/theme-chalk/el-message.css'

let lastMsg = ''
let lastTime = 0

export class log {
  static Log = (options: any) => {
    const now = Date.now()
    if (options.message === lastMsg && now - lastTime < 1500) {
      return
    }
    lastMsg = options.message
    lastTime = now

    ElMessage({
      duration: 1500,
      ...options,
    })
  }
  static error(errMsg: string) {
    if (!errMsg) return
    this.Log({ type: 'error', message: errMsg })
  }
  static info(infoMsg: string) {
    if (!infoMsg) return
    this.Log({ type: 'info', message: infoMsg })
  }
  static success(successMsg: string) {
    if (!successMsg) return
    this.Log({ type: 'success', message: successMsg })
  }
  static warning(warningMsg: string) {
    if (!warningMsg) return
    this.Log({ type: 'warning', message: warningMsg })
  }
}
