<template>
  <div :class="[isDark ? 'dark-theme bg-slate-950 text-slate-100' : 'light-theme bg-slate-50 text-slate-800']" class="login-container min-h-screen w-full flex items-center justify-center relative overflow-hidden transition-all duration-500">
    <!-- 主题切换按钮 -->
    <div class="absolute top-6 right-6 z-20">
      <button 
        @click="isDark = !isDark" 
        class="w-10 h-10 rounded-full flex items-center justify-center border transition-all duration-300 cursor-pointer shadow-md"
        :class="isDark ? 'bg-slate-900 border-slate-800 text-yellow-400 hover:bg-slate-800' : 'bg-white border-slate-200 text-slate-700 hover:bg-slate-50'"
      >
        <el-icon class="text-lg">
          <Sunny v-if="isDark" />
          <Moon v-else />
        </el-icon>
      </button>
    </div>

    <!-- 背景流光和装饰圆球 (提升视觉高级感) -->
    <div :class="isDark ? 'bg-cyan-500/10' : 'bg-cyan-300/30'" class="absolute w-[500px] h-[500px] rounded-full blur-[120px] -top-40 -left-40 animate-pulse duration-[8s] transition-colors duration-500"></div>
    <div :class="isDark ? 'bg-blue-600/10' : 'bg-blue-400/30'" class="absolute w-[600px] h-[600px] rounded-full blur-[150px] -bottom-40 -right-40 animate-pulse duration-[10s] transition-colors duration-500"></div>

    <!-- 登录卡片 -->
    <div :class="isDark ? 'border-slate-800 bg-slate-900/40 shadow-2xl' : 'border-slate-200/80 bg-white/75 shadow-xl shadow-blue-100/20'" class="w-full max-w-[420px] mx-4 p-8 rounded-2xl relative z-10 border backdrop-blur-xl transition-all duration-500 animate-fade-in">
      <div class="text-center mb-8">
        <div class="inline-flex items-center justify-center p-3 rounded-xl bg-gradient-to-br from-cyan-500/20 to-blue-600/20 border border-cyan-500/30 mb-4">
          <svg class="w-8 h-8 text-cyan-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
          </svg>
        </div>
        <h2 class="text-2xl font-bold tracking-tight transition-colors duration-500">STP 管理员系统</h2>
        <p :class="isDark ? 'text-slate-400' : 'text-slate-500'" class="text-sm mt-2 font-light transition-colors duration-500">请输入管理员凭证以访问后台系统</p>
      </div>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="LoginRules"
        label-width="0"
        class="space-y-5"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="管理员账号"
            :prefix-icon="User"
            class="admin-custom-input"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            :prefix-icon="Lock"
            show-password
            class="admin-custom-input"
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <div class="flex items-center justify-between text-sm mt-2">
          <el-checkbox v-model="rememberMe" class="admin-checkbox">记住登录状态</el-checkbox>
        </div>

        <div class="pt-2">
          <el-button
            type="primary"
            class="w-full h-11 rounded-lg text-base font-semibold bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-600 hover:to-blue-700 border-none transition-all duration-300 transform active:scale-[0.98] cursor-pointer shadow-lg shadow-cyan-500/20"
            :loading="loading"
            @click="handleLogin"
          >
            登录后台
          </el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {reactive, ref} from 'vue'
import {useRouter} from 'vue-router'
import {type FormInstance, type FormRules} from 'element-plus'
import {Lock, User, Sunny, Moon} from '@element-plus/icons-vue'
import {useAuthStore} from '@/views/auth/store'
import type {LoginForm} from '@/views/auth/types'
import {Auther} from '@/views/auth/composables/Auth'
import {useUserInfoStore} from '@/stores/userInfo'

const router = useRouter()
const authStore = useAuthStore()
const auther = Auther.getInstance()

const loginFormRef = ref<FormInstance>()
const loading = ref(false)
const rememberMe = ref(true)
const isDark = ref(false)

const loginForm: LoginForm = reactive({
  username: '',
  password: '',
  loginType: 'password'
})

const LoginRules = reactive<FormRules>({
  username: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
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
          // 登录成功后拉取用户信息
          const userInfoStore = useUserInfoStore()
          await userInfoStore.flush()
          router.push('/')
        }
      } catch {
        // 错误在 Axios 响应拦截器统一集中处理
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style>
/* 针对 Element Plus 输入框的精细深度定制，排除 Demo 感 */
.admin-custom-input .el-input__wrapper {
  background-color: rgba(15, 23, 42, 0.6) !important;
  box-shadow: 0 0 0 1px rgba(51, 65, 85, 0.5) inset !important;
  border-radius: 8px !important;
  padding: 8px 12px !important;
  transition: all 0.3s ease !important;
}

.admin-custom-input .el-input__wrapper:hover {
  box-shadow: 0 0 0 1px rgba(6, 182, 212, 0.4) inset !important;
}

.admin-custom-input .el-input__wrapper.is-focus {
  box-shadow: 0 0 0 1px rgba(6, 182, 212, 0.8) inset, 0 0 12px rgba(6, 182, 212, 0.15) !important;
}

.admin-custom-input .el-input__inner {
  color: #f1f5f9 !important;
  font-size: 14px !important;
}

.admin-custom-input .el-input__inner::placeholder {
  color: #64748b !important;
}

.admin-checkbox .el-checkbox__label {
  color: #94a3b8 !important;
}

.admin-checkbox .el-checkbox__inner {
  background-color: rgba(15, 23, 42, 0.6) !important;
  border-color: rgba(51, 65, 85, 0.6) !important;
}

.admin-checkbox .is-checked .el-checkbox__inner {
  background-color: #06b6d4 !important;
  border-color: #06b6d4 !important;
}

/* 亮色主题额外定制 */
.light-theme .admin-custom-input .el-input__wrapper {
  background-color: rgba(255, 255, 255, 0.8) !important;
  box-shadow: 0 0 0 1px rgba(203, 213, 225, 0.8) inset !important;
}

.light-theme .admin-custom-input .el-input__wrapper:hover {
  box-shadow: 0 0 0 1px rgba(6, 182, 212, 0.5) inset !important;
}

.light-theme .admin-custom-input .el-input__wrapper.is-focus {
  box-shadow: 0 0 0 1px rgba(6, 182, 212, 0.8) inset, 0 0 12px rgba(6, 182, 212, 0.15) !important;
  background-color: #ffffff !important;
}

.light-theme .admin-custom-input .el-input__inner {
  color: #1e293b !important;
}

.light-theme .admin-custom-input .el-input__inner::placeholder {
  color: #94a3b8 !important;
}

.light-theme .admin-checkbox .el-checkbox__label {
  color: #475569 !important;
}

.light-theme .admin-checkbox .el-checkbox__inner {
  background-color: rgba(255, 255, 255, 0.8) !important;
  border-color: rgba(203, 213, 225, 0.8) !important;
}

.light-theme .admin-checkbox .is-checked .el-checkbox__inner {
  background-color: #06b6d4 !important;
  border-color: #06b6d4 !important;
}
</style>
