import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Auth } from './types'

export const useAuthStore = defineStore('auth', () => {
  const token = ref('')
  const refreshToken = ref('')
  const username = ref('')

  const setAuth = (data: Partial<Auth>) => {
    if (data.token) {
      token.value = data.token
    }
    if (data.refreshToken) {
      refreshToken.value = data.refreshToken
    }
    if ('username' in data && data.username) {
      username.value = data.username
    }
  }

  const clearAuth = () => {
    token.value = ''
    refreshToken.value = ''
    username.value = ''
  }

  return {
    token,
    refreshToken,
    username,
    setAuth,
    clearAuth,
  }
}, {
  persist: true
})
