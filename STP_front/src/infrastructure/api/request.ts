import axios from 'axios'
import type { InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import type { Result } from '@/shared/types/result'
import { useAuthStore } from '@/shared/stores/auth'
import { log } from '../utils/Log'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000,
})

let isRefreshing = false
let requestsQueue: any[] = []

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
        // 防止刷新 Token 接口本身失败进入无限死循环
        if (config.url && config.url.includes('/refresh-token')) {
          const authStore = useAuthStore()
          authStore.clearAuth()
          window.location.href = '/auth/login'
          return Promise.reject(new Error('登录已过期'))
        }
        return handleTokenRefresh(config)
      }

      log.error(res.errMsg || 'Unknown Error')
      return Promise.reject(new Error(res.errMsg || 'Error'))
    }
    // 统一返回 response.data，使用户调用 API 时直接获取 Result 对象
    return response.data as any
  },
  async (error) => {
    const { response, config } = error

    // 防止刷新 Token 接口本身失败进入无限死循环
    if (config && config.url && config.url.includes('/refresh-token')) {
      const authStore = useAuthStore()
      authStore.clearAuth()
      window.location.href = '/auth/login'
      return Promise.reject(error)
    }

    // 如果 HTTP 状态码是 401，也尝试刷新
    if (response && response.status === 401) {
      return handleTokenRefresh(config)
    }

    ElMessage.error(error.message || 'Network Error')
    return Promise.reject(error)
  },
)

async function handleTokenRefresh(config: InternalAxiosRequestConfig) {
  const authStore = useAuthStore()

  // 如果根本没有 refreshToken，直接判定过期并跳转，避免发送无意义的刷新请求
  if (!authStore.refreshToken) {
    authStore.clearAuth()
    window.location.href = '/auth/login'
    return Promise.reject(new Error('未登录或登录状态已失效'))
  }

  if (!isRefreshing) {
    isRefreshing = true
    try {
      // 动态导入避免循环依赖
      const { Auther } = await import('../../auth/api/hooks/Auth')
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

      // 改为 History 模式跳转
      window.location.href = '/auth/login'
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
