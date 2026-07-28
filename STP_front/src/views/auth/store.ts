import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Auth } from './types'

export const useAuthStore = defineStore(
  'auth',
  () => {
    const token = ref('')
    const refreshToken = ref('')
    const username = ref('')

    const loginDialogVisible = ref(false)

    const setAuth = (data: Partial<Auth>) => {
      if (data.token) {
        token.value = data.token
      }
      if (data.refreshToken) {
        refreshToken.value = data.refreshToken
      }
      if (data.username) {
        username.value = data.username
      }
    }

    const clearAuth = () => {
      token.value = ''
      refreshToken.value = ''
      username.value = ''
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
    persist: {
      pick: ['token', 'refreshToken', 'username']
    },
  },
)
