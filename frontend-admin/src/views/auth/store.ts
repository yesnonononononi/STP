import {defineStore} from 'pinia'
import {ref} from 'vue'
import type {Auth} from './types'

export const useAuthStore = defineStore(
  'auth',
  () => {
    const token = ref(localStorage.getItem('admin_token') || '')
    const refreshToken = ref(localStorage.getItem('admin_refresh_token') || '')
    const username = ref(localStorage.getItem('admin_username') || '')

    const loginDialogVisible = ref(false)

    const setAuth = (data: Partial<Auth>) => {
      if (data.token) {
        token.value = data.token
        localStorage.setItem('admin_token', data.token)
      }
      if (data.refreshToken) {
        refreshToken.value = data.refreshToken
        localStorage.setItem('admin_refresh_token', data.refreshToken)
      }
      if (data.username) {
        username.value = data.username
        localStorage.setItem('admin_username', data.username)
      }
    }

    const clearAuth = () => {
      token.value = ''
      refreshToken.value = ''
      username.value = ''
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_refresh_token')
      localStorage.removeItem('admin_username')
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
    persist: true,
  },
)
