import type { AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import axios from 'axios'
import type { Result } from '@/types/result'
import { useAuthStore } from '@/views/auth/store'
import { log } from '@/utils/log'

const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'
const REFRESH_TOKEN_PATH = '/user-auth/refresh-token'
const LOGOUT_PATH = '/user-auth/logout'
const AUTH_ENDPOINTS = [
  '/user-auth/login',
  '/user-auth/register',
  '/user-auth/forget',
  '/user-auth/logout',
  REFRESH_TOKEN_PATH,
] as const

type RetryableRequestConfig = InternalAxiosRequestConfig & {
  _tokenRefreshRetried?: boolean
}

const request = axios.create({
  baseURL,
  timeout: 10000,
})

const GUEST_DEVICE_ID_KEY = 'stp_guest_device_id'

const getGuestDeviceId = (): string => {
  if (typeof window === 'undefined') {
    return ''
  }

  try {
    const existing = window.localStorage.getItem(GUEST_DEVICE_ID_KEY)
    if (existing) {
      return existing
    }

    const deviceId =
      window.crypto?.randomUUID?.() || `${Date.now()}-${Math.random().toString(36).slice(2)}`
    window.localStorage.setItem(GUEST_DEVICE_ID_KEY, deviceId)
    return deviceId
  } catch {
    return ''
  }
}

// 同一时间只允许一个刷新请求。refresh token 轮换后，其他 401 请求复用这个 Promise，
// 防止并发请求各自消费同一个旧 refresh token。
let refreshPromise: Promise<string> | null = null

const isRefreshTokenRequest = (config?: InternalAxiosRequestConfig) =>
  Boolean(config?.url?.includes(REFRESH_TOKEN_PATH))

const isLogoutRequest = (config?: InternalAxiosRequestConfig) =>
  Boolean(config?.url?.includes(LOGOUT_PATH))

const isAuthEndpoint = (config?: InternalAxiosRequestConfig) =>
  Boolean(config?.url && AUTH_ENDPOINTS.some((path) => config.url?.includes(path)))

const invalidateAuth = (message: string) => {
  const authStore = useAuthStore()
  authStore.clearAuth()
  if (!authStore.loginDialogVisible) {
    authStore.showLoginDialog()
  }
  return Promise.reject(new Error(message))
}

const refreshAccessToken = (): Promise<string> => {
  if (refreshPromise) {
    return refreshPromise
  }

  const authStore = useAuthStore()
  const username = authStore.username
  const currentRefreshToken = authStore.refreshToken
  if (!username || !currentRefreshToken) {
    return invalidateAuth('未登录或登录状态已失效')
  }

  refreshPromise = (async () => {
    try {
      // CommonAPI 使用独立 Axios 实例，刷新请求不会再次进入这里，避免递归刷新。
      const { CommonAPI } = await import('@/services/common/api')
      const result = await CommonAPI.refreshToken({
        username,
        refreshToken: currentRefreshToken,
      })
      const newData = result?.data

      if (result?.code !== 1 || !newData?.token || !newData?.refreshToken) {
        throw new Error(result?.errMsg || '登录状态已失效')
      }

      // 后端刷新时会同时轮换 access/refresh token，必须一次性持久化两者。
      authStore.setAuth(newData)
      return newData.token
    } catch (error) {
      authStore.clearAuth()
      if (!authStore.loginDialogVisible) {
        authStore.showLoginDialog()
      }
      throw error
    } finally {
      refreshPromise = null
    }
  })()

  return refreshPromise
}

const handleUnauthorized = async (config: InternalAxiosRequestConfig, error?: unknown) => {
  const retryableConfig = config as RetryableRequestConfig

  // refresh-token 失败不能再次刷新；登录/注册/找回密码/登出失败也不应该消费旧 refresh token。
  if (isRefreshTokenRequest(config)) {
    return invalidateAuth('登录状态已失效')
  }
  if (isAuthEndpoint(config)) {
    return Promise.reject(error || new Error('认证请求失败'))
  }

  // 原请求只允许重试一次，防止后端持续返回 401 时形成请求循环。
  if (retryableConfig._tokenRefreshRetried) {
    return invalidateAuth('登录状态已失效')
  }

  try {
    const token = await refreshAccessToken()
    retryableConfig._tokenRefreshRetried = true
    retryableConfig.headers = retryableConfig.headers || {}
    retryableConfig.headers.Authorization = `Bearer ${token}`
    return request(retryableConfig)
  } catch (refreshError) {
    return Promise.reject(refreshError)
  }
}

request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const authStore = useAuthStore()
    // auth 模块的登出接口同时要求 Authorization 和 X-Refresh-Token 请求头。
    // 只给登出请求附加 refresh token，避免把 refresh token 泄漏到普通业务接口。
    if (authStore.token && config.headers) {
      config.headers.Authorization = `Bearer ${authStore.token}`
    }
    if (!authStore.token && config.headers) {
      const deviceId = getGuestDeviceId()
      if (deviceId) {
        config.headers['X-Device-Id'] = deviceId
      }
    }
    if (isLogoutRequest(config) && authStore.refreshToken && config.headers) {
      config.headers['X-Refresh-Token'] = authStore.refreshToken
    }
    return config
  },
  (error) => Promise.reject(error),
)

request.interceptors.response.use(
  async (response: AxiosResponse<Result>) => {
    const res = response.data
    const config = response.config

    // 后端 Result.success 的 code 为 1；业务 401 与 HTTP 401 走同一套刷新流程。
    if (res?.code !== 1) {
      if (res?.code === 401) {
        return handleUnauthorized(config)
      }
      const message = res?.errMsg || '服务繁忙'
      log.error(message)
      return Promise.reject(new Error(message))
    }

    return response.data as any
  },
  async (error) => {
    const { response, config } = error || {}
    if (response?.status === 401 && config) {
      return handleUnauthorized(config, error)
    }
    return Promise.reject(error)
  },
)

export default request
