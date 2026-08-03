import axios from 'axios'
import type { InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import type { Result } from '@/types/result'
import { useAuthStore } from '@/views/auth/store'
import { log } from '@/utils/log'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000,
})

let isRefreshing = false
let requestsQueue: any[] = []

// 处理未授权（401）逻辑：执行刷新 Token 或防死循环直接跳转登录
const handleUnauthorized = async (config: InternalAxiosRequestConfig, error?: any) => {
  const authStore = useAuthStore()
  if (config.url && config.url.includes('/refresh-token')) {
    authStore.clearAuth()
    authStore.showLoginDialog()
    return Promise.reject(error || new Error('登录已过期'))
  }
  return handleTokenRefresh(config)
}

// Request interceptor
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const authStore = useAuthStore()
    if (authStore.token && config.headers) {
      config.headers.Authorization = `Bearer ${authStore.token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  },
)

// Response interceptor
request.interceptors.response.use(
  async (response: AxiosResponse<Result>) => {
    const res = response.data
    const config = response.config

    // Backend: 1 success, 0 error
    if (res.code !== 1) {
      if (res.code === 401) {
        return handleUnauthorized(config)
      }
      log.error(res.errMsg || '服务繁忙')
      return Promise.reject(new Error(res.errMsg || 'Error'))
    }

    // 统一返回 response.data，使用户调用 API 时直接获取 Result 对象
    return response.data as any
  },
  async (error) => {
    const { response, config } = error

    if (response && response.status === 401 && config) {
      return handleUnauthorized(config, error)
    }

    return Promise.reject(error)
  },
)

async function handleTokenRefresh(config: InternalAxiosRequestConfig) {
  const authStore = useAuthStore()

  // 如果根本没有 refreshToken，直接判定过期并跳转，避免发送无意义的刷新请求
  if (!authStore.refreshToken) {
    authStore.clearAuth()
    authStore.showLoginDialog()
    return Promise.reject(new Error('未登录或登录状态已失效'))
  }

  if (!isRefreshing) {
    isRefreshing = true
    try {
      // 动态导入避免循环依赖
      const { Auther } = await import('@/views/auth/composables/Auth')
      const res = await Auther.getInstance().refreshToken({
        username: authStore.username,
        refreshToken: authStore.refreshToken,
      })

      const newData = res.data
      if (!newData) {
        throw new Error('Token refresh failed')
      }
      authStore.setAuth(newData)

      // 重新执行队列中的请求
      requestsQueue.forEach((cb) => cb(newData.token))
      requestsQueue = []

      // 重新执行当前请求
      if (config.headers) {
        config.headers.Authorization = `Bearer ${newData.token}`
      }
      return request(config)
    } catch (err) {
      authStore.clearAuth()

      // 核心修复：如果刷新 Token 失败，拒绝队列中所有挂起的请求，防止页面无限 loading 挂死
      requestsQueue.forEach((cb) => cb('', err))
      requestsQueue = []

      authStore.showLoginDialog()
      return Promise.reject(err)
    } finally {
      isRefreshing = false
    }
  } else {
    // 正在刷新，将请求存入队列（支持 resolve 和 reject 双重回调）
    return new Promise((resolve, reject) => {
      requestsQueue.push((token: string, err?: any) => {
        if (err) {
          reject(err)
        } else {
          if (config.headers) {
            config.headers.Authorization = `Bearer ${token}`
          }
          resolve(request(config))
        }
      })
    })
  }
}

export default request
