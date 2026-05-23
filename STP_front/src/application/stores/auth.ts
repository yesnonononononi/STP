import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Auth } from '../types/Auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const refreshToken = ref(localStorage.getItem('refreshToken') || '')
  const username = ref(localStorage.getItem('username') || '')

  const setAuth = (data: Partial<Auth>) => {
    if (data.token) {
      token.value = data.token
      localStorage.setItem('token', data.token)
    }
    if (data.refreshToken) {
      refreshToken.value = data.refreshToken
      localStorage.setItem('refreshToken', data.refreshToken)
    }
    if ('username' in data && data.username) {
      username.value = data.username
      localStorage.setItem('username', data.username)
    }
  }

  const clearAuth = () => {
    token.value = ''
    refreshToken.value = ''
    username.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('username')
  }

  return {
    token,
    refreshToken,
    username,
    setAuth,
    clearAuth,
  }
})
