import type { Result } from '@/types/result'
import request from '@/services/request'
import type { UploadVO } from './types'

export class CommonAPI {
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
   * 上传大文件如视频
   */
  static async uploadLargeFile(
    file: File,
    folder: string | null,
  ): Promise<Result<UploadVO | void>> {
    const chunkSize = 5 * 1024 * 1024 // 5MB 分片大小
    const totalChunk = Math.ceil(file.size / chunkSize) // 计算分片数量
    // 1. 分片上传
    for (let i = 0; i < totalChunk; i++) {
      const chunk = file.slice(i * chunkSize, (i + 1) * chunkSize)
      const formData = new FormData()
      formData.append('folder', folder || '')
      formData.append('chunk', totalChunk + '' || '')
      formData.append('file', chunk, file.name)
      formData.append('curIndex', i + '' || '')

      const result: Result = await request.post('/common/upload/chunk', formData, {
        timeout: 600000,
      })
      if (result.code !== 1) {
        console.log(`分片${i}/${totalChunk}上传失败 ${result.errMsg}`)
        return result
      }
      console.log(`分片${i}/${totalChunk}上传成功`)
    }

    // 2. 发送合并请求 (curIndex = totalChunk)
    const mergeData = new FormData()
    mergeData.append('folder', folder || '')
    mergeData.append('chunk', totalChunk + '' || '')
    mergeData.append('curIndex', totalChunk + '')
    mergeData.append('file', new Blob(), file.name)

    console.log('正在发送分片合并请求...')
    return await request.post('/common/upload/chunk', mergeData, {
      timeout: 600000,
    })
  }
}
