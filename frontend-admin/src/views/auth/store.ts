import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Auth } from './types'

const ACCESS_TOKEN_KEY = 'admin_token'
const USERNAME_KEY = 'admin_username'
// 历史版本曾把 refresh token 写入 localStorage，登出时需一并清理。
const LEGACY_REFRESH_TOKEN_KEY = 'admin_refresh_token'

export const useAuthStore = defineStore(
  'auth',
  () => {
    // access token 有效期短，可保留在 localStorage 以支持刷新页面后仍保持登录态。
    const token = ref(localStorage.getItem(ACCESS_TOKEN_KEY) || '')
    // refresh token 仅保存在内存中，绝不落盘到 localStorage，降低 XSS 场景下的令牌窃取风险。
    // 安全目标：refresh token 应由后端通过 HttpOnly + Secure + SameSite Cookie 下发并自动携带，
    // 前端不接触其明文。当前后端（/user-auth/refresh-token）尚未支持 Cookie 下发，
    // 因此这里先退化为“仅内存保存”，代价是页面刷新后需重新登录，待后端支持后改为纯 Cookie 方案。
    const refreshToken = ref('')
    const username = ref(localStorage.getItem(USERNAME_KEY) || '')

    const loginDialogVisible = ref(false)

    const setAuth = (data: Partial<Auth>) => {
      if (data.token) {
        token.value = data.token
        localStorage.setItem(ACCESS_TOKEN_KEY, data.token)
      }
      if (data.refreshToken) {
        // 仅内存保存，不写入 localStorage。
        refreshToken.value = data.refreshToken
      }
      if (data.username) {
        username.value = data.username
        localStorage.setItem(USERNAME_KEY, data.username)
      }
    }

    const clearAuth = () => {
      token.value = ''
      refreshToken.value = ''
      username.value = ''
      localStorage.removeItem(ACCESS_TOKEN_KEY)
      localStorage.removeItem(USERNAME_KEY)
      localStorage.removeItem(LEGACY_REFRESH_TOKEN_KEY)
    }

    const showLoginDialog = () => {
      loginDialogVisible.value = true
    }

    const hideLoginDialog = () => {
      loginDialogVisible.value = false
    }

    return {
      token,
      refreshToken,
      username,
      loginDialogVisible,
      setAuth,
      clearAuth,
      showLoginDialog,
      hideLoginDialog,
    }
  },
  {
    // 只持久化 access token 与用户名；refresh token 明确排除在持久化之外。
    persist: {
      key: 'auth',
      pick: ['token', 'username'],
    },
  },
)
