import { ElMessage } from 'element-plus'

export class log {
  static Log = (options: any) => {
    ElMessage({
      duration: 1500,
      ...options,
    })
  }
  static error(errMsg: string) {
    this.Log({ type: 'error', message: errMsg })
  }
  static info(infoMsg: string) {
    this.Log({ type: 'info', message: infoMsg })
  }
  static success(successMsg: string) {
    this.Log({ type: 'success', message: successMsg })
  }
  static arning(warningMsg: string) {
    this.Log({ type: 'warning', message: warningMsg })
  }
}
