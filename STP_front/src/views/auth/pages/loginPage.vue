<template>
  <div class="w-full animate-fade-in">
    <el-form
      ref="loginFormRef"
      :model="loginForm"
      :rules="LoginRules"
      label-width="0"
      class="space-y-4"
    >
      <el-form-item prop="username">
        <el-input
          v-model="loginForm.username"
          placeholder="请输入用户名"
          :prefix-icon="User"
          class="custom-input"
        />
      </el-form-item>

      <el-form-item prop="password">
        <el-input
          v-model="loginForm.password"
          type="password"
          placeholder="请输入密码"
          :prefix-icon="Lock"
          show-password
          class="custom-input"
          @keyup.enter="handleLogin"
        />
      </el-form-item>

      <div class="flex flex-col gap-4 mt-6">
        <el-button
          type="primary"
          class="w-full h-10 rounded-lg text-base font-semibold bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-600 hover:to-blue-700 border-none transition-all duration-300 transform active:scale-[0.98]"
          :loading="loading"
          @click="handleLogin"
        >
          登录
        </el-button>

        <div class="flex justify-between items-center text-sm mt-2">
          <router-link
            class="text-slate-400 hover:text-cyan-400 transition-colors"
            to="/auth/forget"
            >忘记密码？</router-link
          >
          <router-link
            class="text-cyan-400 hover:text-cyan-300 font-medium transition-colors"
            to="/auth/register"
            >注册账号</router-link
          >
        </div>
      </div>
    </el-form>
  </div>
</template>

<script lang="ts" setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { type FormInstance, type FormRules } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useAuthStore } from '@/views/auth/store'
import type { LoginForm } from '@/views/auth/types'
import { Auther } from '@/views/auth/composables/Auth'
import { useUserInfoStore } from '@/stores/userInfo'

const router = useRouter()
const authStore = useAuthStore()
const auther = Auther.getInstance()

const loginFormRef = ref<FormInstance>()
const loading = ref(false)

const loginForm: LoginForm = reactive({
  username: '',
  password: '',
})

const LoginRules = reactive<FormRules>({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
})

const handleLogin = async () => {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await auther.login(loginForm)
        if (res.data) {
          authStore.setAuth(res.data)
          // 登录成功后立即获取最新的用户信息并缓存
          await useUserInfoStore().flush()
        }
        router.push('/')
      } catch (err: any) {
        console.error(err)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style>
/* 输入框及相关样式已通过全局或者共享结构在 registerPage 中进行声明，可以直接复用 */
</style>
