import { UserAPI, type UserProfileData } from '@/services/user'
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserInfoStore = defineStore(
  'userInfo',
  () => {
    // State - 用户基本信息
    const user = ref<UserProfileData | null>(null)

    // State - 加载状态
    const loading = ref(false)

    // State - 错误信息
    const error = ref<string | null>(null)

    // Getters - 计算属性
    const isLoggedIn = computed(() => !!user.value)
    const username = computed(() => user.value?.nick || '')
    const avatar = computed(() => user.value?.avatar || '')
    const userId = computed(() => user.value?.id || '')

    // Actions - 设置用户信息
    const setUser = (userData: UserProfileData) => {
      user.value = userData
      error.value = null
    }
    const flush = async () => {
      const user = (await UserAPI.getCurrentUser()).data
      if (!user) return
      setUser(user)
    }
    // Actions - 清除用户信息
    const clearUser = () => {
      user.value = null
      error.value = null
    }

    // Actions - 设置加载状态
    const setLoading = (status: boolean) => {
      loading.value = status
    }

    // Actions - 设置错误信息
    const setError = (message: string | null) => {
      error.value = message
    }

    // Actions - 更新用户部分信息
    const updateUserInfo = (updates: Partial<UserProfileData>) => {
      if (user.value) {
        user.value = { ...user.value, ...updates }
      }
    }

    //Actions - 计算用户信息完整度
    const calculateCompleteness = () => {
      const all = 5
      let count = 0
      if (!user.value) return
      count =
        (user.value.nick ? 1 : 0) +
        (user.value.avatar ? 1 : 0) +
        (user.value.gender !== null && user.value.gender !== undefined ? 1 : 0) +
        (user.value.age ? 1 : 0) +
        (user.value.introduction ? 1 : 0)
      return (count / all) * 100
    }

    return {
      // State
      user,
      loading,
      error,

      // Getters
      isLoggedIn,
      username,
      avatar,
      userId,

      // Actions
      setUser,
      clearUser,
      flush,
      setLoading,
      calculateCompleteness,
      setError,
      updateUserInfo,
    }
  },
  {
    persist: true,
  },
)
