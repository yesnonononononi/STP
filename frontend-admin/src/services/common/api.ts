import axios from 'axios'
import type {Result} from '@/types/result'
import request from '@/services/request'
import type {UploadVO} from './types'

const commonRequest = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000,
})

export interface RefreshTokenForm {
  username: string
  refreshToken: string
}

export interface RefreshTokenData {
  token: string
  refreshToken?: string
}

export class CommonAPI {
  /**
   * 刷新访问 Token。
   *
   * 这里必须使用不带认证拦截器的 Axios 实例，避免 refresh-token 请求失败时再次进入
   * request.ts 的 401 刷新流程。
   */
  static async refreshToken(form: RefreshTokenForm): Promise<Result<Partial<RefreshTokenData>>> {
    const response = await commonRequest.post<Result<Partial<RefreshTokenData>>>(
      '/user-auth/refresh-token',
      undefined,
      {
        // auth 模块从请求头读取 refreshToken，从查询参数读取 username，不能放在 JSON body 中。
        params: { username: form.username },
        headers: { 'X-Refresh-Token': form.refreshToken },
      },
    )
    return response.data
  }

  /**
   * 上传媒体文件 (对接后端接口格式与大小校验)
   */
  static async upload(file: File, folder: string | null): Promise<Result<UploadVO>> {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('folder', folder || '')
    return await request.post('/common/upload', formData, { timeout: 600000 })
  }

  /**
   * 上传大文件（高层流程编排入口）
   */
  static async uploadLargeFile(
    file: File,
    folder: string | null,
  ): Promise<Result<UploadVO | void>> {
    const chunkSize = 5 * 1024 * 1024
    const totalChunk = Math.ceil(file.size / chunkSize)

    // 1. 构建分片任务队列并采用 3 并发管道传输
    const tasks = Array.from({ length: totalChunk }, (_, i) => () =>
      this.uploadSingleChunk(file, folder, i, totalChunk, chunkSize),
    )
    await this.runConcurrentTasks(tasks, 3)

    // 2. 发起最终合并请求
    return await this.sendMergeRequest(file, folder, totalChunk)
  }

  /**
   * 上传单个分片私有逻辑
   */
  private static async uploadSingleChunk(
    file: File,
    folder: string | null,
    curIndex: number,
    totalChunk: number,
    chunkSize: number,
  ): Promise<Result> {
    const chunk = file.slice(curIndex * chunkSize, (curIndex + 1) * chunkSize)
    const formData = new FormData()
    formData.append('folder', folder || '')
    formData.append('chunk', totalChunk + '')
    formData.append('file', chunk, file.name)
    formData.append('curIndex', curIndex + '')

    const result: Result = await request.post('/common/upload/chunk', formData, { timeout: 600000 })
    if (result.code !== 1) {
      throw new Error(`分片 ${curIndex}/${totalChunk} 上传失败: ${result.errMsg}`)
    }
    return result
  }

  /**
   * 并发任务池执行工具
   */
  private static async runConcurrentTasks(
    tasks: (() => Promise<any>)[],
    concurrency: number,
  ): Promise<void> {
    let poolIndex = 0
    const workers = Array.from({ length: Math.min(concurrency, tasks.length) }, async () => {
      while (poolIndex < tasks.length) {
        const task = tasks[poolIndex++]
        await task()
      }
    })
    await Promise.all(workers)
  }

  /**
   * 发送合并分片私有请求
   */
  private static async sendMergeRequest(
    file: File,
    folder: string | null,
    totalChunk: number,
  ): Promise<Result<UploadVO>> {
    const mergeData = new FormData()
    mergeData.append('folder', folder || '')
    mergeData.append('chunk', totalChunk + '')
    mergeData.append('curIndex', totalChunk + '')
    mergeData.append('file', new Blob(), file.name)

    return await request.post('/common/upload/chunk', mergeData, { timeout: 600000 })
  }
}

