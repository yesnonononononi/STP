<template>
  <el-dialog
    v-model="authStore.loginDialogVisible"
    width="800px"
    align-center
    class="premium-login-dialog"
    :close-on-click-modal="false"
    destroy-on-close
    :show-close="false"
  >
    <!-- 全局包装容器 -->
    <div class="relative w-full flex min-h-[460px] rounded-2xl overflow-hidden bg-white select-none shadow-2xl">
      <!-- 绝对定位的关闭按钮 -->
      <button 
        @click="authStore.hideLoginDialog" 
        class="absolute top-4 right-4 text-stone-400 hover:text-stone-600 hover:rotate-90 transition-all duration-300 z-50 cursor-pointer"
      >
        <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>

      <!-- 左侧栏：用户认证主通道 -->
      <div class="flex-1 p-10 flex flex-col justify-between min-h-[460px]">
        <!-- 顶部 Logo 与系统标示 -->
        <div class="flex items-center gap-2 mb-4 shrink-0">
          <img src="/logo.png" class="size-7" alt="logo" />
          <span class="text-xl font-bold bg-gradient-to-r from-blue-500 to-indigo-600 bg-clip-text text-transparent">STP 社交网络</span>
        </div>

        <!-- 动态切换的表单内容容器 -->
        <div class="flex-1 flex flex-col justify-center py-4">
          <transition name="fade-slide" mode="out-in">
            <!-- 1. 登录表单 -->
            <div v-if="activeMode === 'login'" key="login" class="w-full">
              <div class="flex gap-4 mb-5 text-sm">
                <span class="font-bold text-slate-800 border-b-2 border-slate-800 pb-1 cursor-pointer">密码登录</span>
              </div>

              <el-form
                ref="loginFormRef"
                :model="loginForm"
                :rules="loginRules"
                label-width="0"
                class="w-full space-y-4"
              >
                <el-form-item prop="username">
                  <el-input
                    v-model="loginForm.username"
                    placeholder="请输入用户名"
                    :prefix-icon="User"
                    class="custom-dialog-input"
                  />
                </el-form-item>

                <el-form-item prop="password">
                  <el-input
                    v-model="loginForm.password"
                    type="password"
                    placeholder="请输入密码"
                    :prefix-icon="Lock"
                    show-password
                    class="custom-dialog-input"
                    @keyup.enter="handleLogin"
                  />
                </el-form-item>

                <!-- 服务协议同意框 -->
                <div class="flex items-center text-xs text-slate-400 pt-1 px-1">
                  <el-checkbox v-model="agreeTerms" class="custom-checkbox">
                    <span class="text-[11px] text-slate-400">
                      我已阅读并同意
                      <span class="text-blue-500 hover:underline cursor-pointer">《用户协议》</span>
                      和
                      <span class="text-blue-500 hover:underline cursor-pointer">《隐私政策》</span>
                    </span>
                  </el-checkbox>
                </div>

                <!-- 登录及辅助按钮 -->
                <div class="flex flex-col gap-3 pt-2">
                  <el-button
                    type="primary"
                    class="w-full h-10 rounded-lg text-sm font-semibold bg-gradient-to-r from-blue-500 to-indigo-600 hover:from-blue-600 hover:to-indigo-700 border-none transition-all duration-300 transform active:scale-[0.98]"
                    :loading="loading"
                    @click="handleLogin"
                  >
                    登录
                  </el-button>

                  <div class="flex justify-between items-center text-xs mt-2 px-1 text-slate-400">
                    <span 
                      class="hover:text-blue-500 cursor-pointer transition-colors"
                      @click="switchMode('forget')"
                    >忘记密码？</span>
                    <span 
                      class="text-blue-500 font-bold hover:text-blue-400 cursor-pointer transition-colors"
                      @click="switchMode('register')"
                    >注册账号</span>
                  </div>
                </div>
              </el-form>
            </div>

            <!-- 2. 注册表单 -->
            <div v-else-if="activeMode === 'register'" key="register" class="w-full">
              <div class="flex gap-4 mb-5 text-sm">
                <span class="font-bold text-slate-800 border-b-2 border-slate-800 pb-1 cursor-pointer">新用户注册</span>
              </div>

              <el-form
                ref="registerFormRef"
                :model="registerForm"
                :rules="registerRules"
                label-width="0"
                class="w-full space-y-4"
              >
                <el-form-item prop="phoneNumber">
                  <el-input
                    v-model="registerForm.phoneNumber"
                    placeholder="请输入手机号"
                    :prefix-icon="Cellphone"
                    class="custom-dialog-input"
                  />
                </el-form-item>

                <el-form-item prop="verifyCode">
                  <div class="flex w-full gap-2">
                    <el-input
                      v-model="registerForm.verifyCode"
                      placeholder="请输入验证码"
                      :prefix-icon="Key"
                      class="custom-dialog-input flex-1"
                    />
                    <el-button
                      type="info"
                      plain
                      class="h-[38px] rounded-lg shrink-0 px-3 text-xs bg-slate-50 border-slate-200 hover:bg-slate-100 hover:text-slate-700 text-slate-500 active:scale-[0.98] transition-all"
                      :disabled="registerCountdown > 0"
                      @click="sendRegisterCode"
                    >
                      {{ registerCountdown > 0 ? `${registerCountdown}s` : '获取验证码' }}
                    </el-button>
                  </div>
                </el-form-item>

                <el-form-item prop="password">
                  <el-input
                    v-model="registerForm.password"
                    type="password"
                    placeholder="请设置密码 (6-20位)"
                    :prefix-icon="Lock"
                    show-password
                    class="custom-dialog-input"
                  />
                </el-form-item>

                <el-form-item prop="confirmPassword">
                  <el-input
                    v-model="registerForm.confirmPassword"
                    type="password"
                    placeholder="请确认密码"
                    :prefix-icon="Lock"
                    show-password
                    class="custom-dialog-input"
                  />
                </el-form-item>

                <div class="flex flex-col gap-3 pt-2">
                  <el-button
                    type="primary"
                    class="w-full h-10 rounded-lg text-sm font-semibold bg-gradient-to-r from-blue-500 to-indigo-600 hover:from-blue-600 hover:to-indigo-700 border-none transition-all duration-300 transform active:scale-[0.98]"
                    :loading="loading"
                    @click="handleRegister"
                  >
                    立即注册
                  </el-button>

                  <div class="flex justify-center items-center text-xs mt-2 text-slate-400">
                    <span>已有账号？</span>
                    <span 
                      class="text-blue-500 font-bold hover:text-blue-400 cursor-pointer transition-colors"
                      @click="switchMode('login')"
                    >立即登录</span>
                  </div>
                </div>
              </el-form>
            </div>

            <!-- 3. 找回密码表单 -->
            <div v-else-if="activeMode === 'forget'" key="forget" class="w-full">
              <div class="flex gap-4 mb-5 text-sm">
                <span class="font-bold text-slate-800 border-b-2 border-slate-800 pb-1 cursor-pointer">找回密码</span>
              </div>

              <el-form
                ref="forgetFormRef"
                :model="forgetForm"
                :rules="forgetRules"
                label-width="0"
                class="w-full space-y-4"
              >
                <el-form-item prop="phoneNumber">
                  <el-input
                    v-model="forgetForm.phoneNumber"
                    placeholder="请输入绑定的手机号"
                    :prefix-icon="Cellphone"
                    class="custom-dialog-input"
                  />
                </el-form-item>

                <el-form-item prop="verifyCode">
                  <div class="flex w-full gap-2">
                    <el-input
                      v-model="forgetForm.verifyCode"
                      placeholder="请输入验证码"
                      :prefix-icon="Key"
                      class="custom-dialog-input flex-1"
                    />
                    <el-button
                      type="info"
                      plain
                      class="h-[38px] rounded-lg shrink-0 px-3 text-xs bg-slate-50 border-slate-200 hover:bg-slate-100 hover:text-slate-700 text-slate-500 active:scale-[0.98] transition-all"
                      :disabled="forgetCountdown > 0"
                      @click="sendForgetCode"
                    >
                      {{ forgetCountdown > 0 ? `${forgetCountdown}s` : '获取验证码' }}
                    </el-button>
                  </div>
                </el-form-item>

                <el-form-item prop="newPassword">
                  <el-input
                    v-model="forgetForm.newPassword"
                    type="password"
                    placeholder="请输入新密码"
                    :prefix-icon="Lock"
                    show-password
                    class="custom-dialog-input"
                  />
                </el-form-item>

                <el-form-item prop="confirmPassword">
                  <el-input
                    v-model="forgetForm.confirmPassword"
                    type="password"
                    placeholder="请再次确认新密码"
                    :prefix-icon="Lock"
                    show-password
                    class="custom-dialog-input"
                  />
                </el-form-item>

                <div class="flex flex-col gap-3 pt-2">
                  <el-button
                    type="primary"
                    class="w-full h-10 rounded-lg text-sm font-semibold bg-gradient-to-r from-blue-500 to-indigo-600 hover:from-blue-600 hover:to-indigo-700 border-none transition-all duration-300 transform active:scale-[0.98]"
                    :loading="loading"
                    @click="handleReset"
                  >
                    重置密码
                  </el-button>

                  <div class="flex justify-center items-center text-xs mt-2 text-slate-400">
                    <span 
                      class="text-blue-500 font-bold hover:text-blue-400 cursor-pointer transition-colors"
                      @click="switchMode('login')"
                    >返回登录</span>
                  </div>
                </div>
              </el-form>
            </div>
          </transition>
        </div>
      </div>

      <!-- 中间的分隔线与 or 文字 -->
      <div class="relative w-[1px] bg-slate-100 flex items-center justify-center shrink-0">
        <span class="absolute px-2 py-1 bg-white text-[11px] text-slate-300 font-medium tracking-widest rounded-full border border-slate-100 transform -translate-y-4 z-10">or</span>
      </div>

      <!-- 右侧栏：扫码安全认证通道 -->
      <div class="flex-1 bg-slate-50/60 p-10 flex flex-col items-center justify-center min-h-[460px] shrink-0">
        <span class="text-base font-bold text-slate-700 mb-6">扫码登录</span>
        
        <!-- 扫码二维码主容器 -->
        <div class="relative w-40 h-40 bg-white p-3 rounded-lg border border-slate-200 shadow-md flex items-center justify-center hover:shadow-lg transition-all duration-300 group">
          <!-- 二维码矢量 SVG -->
          <svg class="w-full h-full text-slate-800" viewBox="0 0 100 100" fill="currentColor">
            <path d="M5 5h30v30H5V5zm4 4v22h22V9H9z"/>
            <path d="M13 13h14v14H13V13zm52-8h30v30h-30V5zm4 4v22h22V9h-22zm4 4h14v14H69V13zM5 65h30v30H5V65zm4 4v22h22V69H9zm4 4h14v14H13V73zm42-8h8v8h-8v-8zm8 8h8v8h-8v-8zm8-8h8v8h-8v-8zm8 8h8v8h-8v-8zm-16 8h8v8h-8v-8zm16 0h8v8h-8v-8zm-8 8h8v8h-8v-8zm-16 0h8v8h-8v-8zm-8 8h8v8h-8v-8zm24 0h8v8h-8v-8zm-8 8h8v8h-8v-8z"/>
          </svg>
          
          <!-- 悬停刷新层 -->
          <div class="absolute inset-0 bg-white/95 rounded-lg opacity-0 group-hover:opacity-100 transition-opacity duration-300 flex flex-col items-center justify-center gap-1.5 cursor-pointer">
            <svg class="w-8 h-8 text-blue-500 animate-spin-slow" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M4 4v5h.582m15.356 2A8.001 8.001 0 1121.21 6.2M20 20v-5h-.581m0 0a8.003 8.003 0 11-15.357-2" />
            </svg>
            <span class="text-[11px] text-slate-500 font-medium">点击刷新二维码</span>
          </div>
        </div>

        <span class="text-[11px] text-slate-400 mt-5 text-center">
          扫码关注 <span class="text-blue-500 font-bold hover:underline cursor-pointer">【STP】</span> 即可登录
        </span>

        <!-- 仿第三方接入平台小图标区 -->
        <div class="flex items-center gap-3.5 mt-8 border-t border-slate-200/50 pt-6 w-full justify-center">
          <span class="w-7 h-7 rounded-full bg-white border border-slate-100 flex items-center justify-center text-slate-400 hover:text-emerald-500 hover:border-emerald-200 hover:shadow-xs cursor-pointer transition-all duration-200" title="微信">
            <svg class="w-4 h-4 fill-current" viewBox="0 0 24 24">
              <path d="M8.2 14.5c.3 0 .5-.2.5-.5s-.2-.5-.5-.5-.5.2-.5.5.2.5.5.5zm3.8 0c.3 0 .5-.2.5-.5s-.2-.5-.5-.5-.5.2-.5.5.2.5.5.5zm-.3-8.8C7.2 5.7 3 8.7 3 12.6c0 2.1 1.3 3.9 3.3 4.9l.4.5-.3 1.1c-.1.2 0 .4.2.4.2 0 .8-.3 1.4-.7.4.1.7.1 1 .1 4.3 0 7.8-2.6 7.8-6.6.1-3.9-3.9-6.7-7.8-6.7zm8 10.3c1.4-.7 2.3-2 2.3-3.5 0-2.9-3-5-6.1-5-1.1 0-2.1.3-3 .7.7.8 1.3 1.7 1.5 2.8 3.3.4 6.3 2.5 6.3 5.7 0 1.5-1 2.8-2.5 3.5l-.3.4.2.9c0 .1 0 .2-.1.2h-.1c-.2 0-.7-.2-1.1-.5-.3.1-.6.1-.9.1 0 .6-.2 1.3-.6 1.9 2.5-.1 4.9-1.7 5.1-2.4zm-5.3-1.8c.2 0 .4-.2.4-.4s-.2-.4-.4-.4-.4.2-.4.4.2.4.4.4zm2.5 0c.2 0 .4-.2.4-.4s-.2-.4-.4-.4-.4.2-.4.4.2.4.4.4z"/>
            </svg>
          </span>
          <span class="w-7 h-7 rounded-full bg-white border border-slate-100 flex items-center justify-center text-slate-400 hover:text-blue-500 hover:border-blue-200 hover:shadow-xs cursor-pointer transition-all duration-200" title="QQ">
            <svg class="w-4 h-4 fill-current" viewBox="0 0 24 24">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm4.18 13.92c-.37.38-.85.58-1.33.58-.29 0-.58-.08-.84-.24-.26-.16-.48-.39-.63-.67a3.02 3.02 0 0 1-.36-.93 2.9 2.9 0 0 1 .05-1c.08-.29.22-.56.42-.78.37-.38.85-.58 1.33-.58.29 0 .58.08.84.24.26.16.48.39.63.67.15.28.27.59.36.93.09.34.07.68-.05 1-.08.29-.22.56-.42.78zM9.82 13.92c-.37-.38-.51-.9-.42-1.42.08-.34.2-.65.36-.93.15-.28.37-.51.63-.67.26-.16.55-.24.84-.24.48 0 .96.2 1.33.58.2.22.34.49.42.78.09.34.07.68-.05 1-.15.28-.37.51-.63.67-.26.16-.55.24-.84.24-.48 0-.96-.2-1.33-.58z"/>
            </svg>
          </span>
          <span class="w-7 h-7 rounded-full bg-white border border-slate-100 flex items-center justify-center text-slate-400 hover:text-slate-800 hover:border-slate-300 hover:shadow-xs cursor-pointer transition-all duration-200" title="GitHub">
            <svg class="w-4 h-4 fill-current" viewBox="0 0 24 24">
              <path d="M12 2A10 10 0 0 0 2 12c0 4.42 2.87 8.17 6.84 9.5.5.08.66-.23.66-.5v-1.69c-2.77.6-3.36-1.34-3.36-1.34-.46-1.16-1.11-1.47-1.11-1.47-.9-.62.07-.6.07-.6 1 .07 1.53 1.03 1.53 1.03.9 1.52 2.34 1.07 2.91.83.1-.65.35-1.09.63-1.34-2.22-.25-4.55-1.11-4.55-4.92 0-1.11.38-2 1.03-2.71-.1-.25-.45-1.29.1-2.64 0 0 .84-.27 2.75 1.02.79-.22 1.65-.33 2.5-.33.85 0 1.71.11 2.5.33 1.91-1.29 2.75-1.02 2.75-1.02.55 1.35.2 2.39.1 2.64.65.71 1.03 1.6 1.03 2.71 0 3.82-2.34 4.66-4.57 4.91.36.31.69.92.69 1.85V21c0 .27.16.59.67.5C19.14 20.16 22 16.42 22 12A10 10 0 0 0 12 2z"/>
            </svg>
          </span>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script lang="ts" setup>
import {onUnmounted, reactive, ref} from 'vue'
import {type FormInstance, type FormRules} from 'element-plus'
import {Cellphone, Key, Lock, User} from '@element-plus/icons-vue'
import {useAuthStore} from '@/views/auth/store'
import type {LoginForm} from '@/views/auth/types'
import {Auther} from '@/views/auth/composables/Auth'
import {useUserInfoStore} from '@/stores/userInfo'
import {log} from '@/utils/log'

// 1. 全局状态
const authStore = useAuthStore()
const auther = Auther.getInstance()
const userInfoStore = useUserInfoStore()

const activeMode = ref<'login' | 'register' | 'forget'>('login')
const loading = ref(false)
const agreeTerms = ref(false)

// 2. 表单实例与校验定义
const loginFormRef = ref<FormInstance>()
const registerFormRef = ref<FormInstance>()
const forgetFormRef = ref<FormInstance>()

// 3. 各表单数据模型
const loginForm: LoginForm = reactive({
  username: '',
  password: '',
})

const registerForm = reactive({
  phoneNumber: '',
  verifyCode: '',
  password: '',
  confirmPassword: '',
})

const forgetForm = reactive({
  phoneNumber: '',
  verifyCode: '',
  newPassword: '',
  confirmPassword: '',
})

// 4. 定时器相关
const registerCountdown = ref(0)
const forgetCountdown = ref(0)
let registerTimer: any = null
let forgetTimer: any = null

// 5. 校验方法
const validateRegisterConfirm = (rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const validateForgetConfirm = (rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入新密码'))
  } else if (value !== forgetForm.newPassword) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const loginRules = reactive<FormRules>({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
})

const registerRules = reactive<FormRules>({
  phoneNumber: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' },
  ],
  verifyCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符之间', trigger: 'blur' },
  ],
  confirmPassword: [{ required: true, validator: validateRegisterConfirm, trigger: 'blur' }],
})

const forgetRules = reactive<FormRules>({
  phoneNumber: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' },
  ],
  verifyCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符之间', trigger: 'blur' },
  ],
  confirmPassword: [{ required: true, validator: validateForgetConfirm, trigger: 'blur' }],
})

// 6. 行为函数
const switchMode = (mode: 'login' | 'register' | 'forget') => {
  activeMode.value = mode
  // 切换模式时重置表单校验与状态
  loginFormRef.value?.resetFields()
  registerFormRef.value?.resetFields()
  forgetFormRef.value?.resetFields()
}

// 7. 发送验证码逻辑
const sendRegisterCode = () => {
  if (!registerForm.phoneNumber || !/^1[3-9]\d{9}$/.test(registerForm.phoneNumber)) {
    log.warning('请先输入有效的手机号')
    return
  }
  log.success('验证码已发送（测试模式：123456）')
  registerCountdown.value = 60
  registerTimer = setInterval(() => {
    if (registerCountdown.value > 0) {
      registerCountdown.value--
    } else {
      clearInterval(registerTimer)
    }
  }, 1000)
}

const sendForgetCode = () => {
  if (!forgetForm.phoneNumber || !/^1[3-9]\d{9}$/.test(forgetForm.phoneNumber)) {
    log.warning('请先输入有效的手机号')
    return
  }
  log.success('重置验证码已发送（测试模式：123456）')
  forgetCountdown.value = 60
  forgetTimer = setInterval(() => {
    if (forgetCountdown.value > 0) {
      forgetCountdown.value--
    } else {
      clearInterval(forgetTimer)
    }
  }, 1000)
}

// 8. 提交交互
const handleLogin = async () => {
  if (!agreeTerms.value) {
    log.warning('请阅读并勾选同意服务协议及隐私条款')
    return
  }
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await auther.login(loginForm)
        if (res.data) {
          authStore.setAuth(res.data)
          await userInfoStore.flush()
          log.success('登录验证成功')
          authStore.hideLoginDialog()
        }
      } catch (err: any) {
        console.error(err)
        log.error(err.message || '登录失败，请检查账号密码')
      } finally {
        loading.value = false
      }
    }
  })
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await auther.register({
          phoneNumber: registerForm.phoneNumber,
          verifyCode: registerForm.verifyCode,
          password: registerForm.password,
        })
        log.success('注册成功！已自动切换为登录模式')
        // 自动将手机号填入登录表单，并切回登录页
        loginForm.username = registerForm.phoneNumber
        switchMode('login')
      } catch (err: any) {
        console.error(err)
        log.error(err.message || '注册失败，请稍后重试')
      } finally {
        loading.value = false
      }
    }
  })
}

const handleReset = async () => {
  if (!forgetFormRef.value) return
  await forgetFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await auther.forget({
          phoneNumber: forgetForm.phoneNumber,
          verifyCode: forgetForm.verifyCode,
          password: forgetForm.newPassword,
          resetType: 1001,
        })
        log.success('密码重置成功！请用新密码登录')
        // 自动将手机号填入登录表单，并切回登录页
        loginForm.username = forgetForm.phoneNumber
        switchMode('login')
      } catch (err: any) {
        console.error(err)
        log.error(err.message || '重置失败，请检查验证码')
      } finally {
        loading.value = false
      }
    }
  })
}

onUnmounted(() => {
  if (registerTimer) clearInterval(registerTimer)
  if (forgetTimer) clearInterval(forgetTimer)
})
</script>

<style scoped>
/* 深度改写 Element Plus Dialog 整体风格 */
.premium-login-dialog :deep(.el-dialog) {
  padding: 0;
  border-radius: 16px;
  overflow: hidden;
  background-color: transparent;
  box-shadow: none;
}

.premium-login-dialog :deep(.el-dialog__header) {
  display: none;
}

.premium-login-dialog :deep(.el-dialog__body) {
  padding: 0;
}

/* 输入框微调 */
.custom-dialog-input :deep(.el-input__wrapper) {
  border-radius: 8px;
  padding: 8px 12px;
  background-color: #f8fafc;
  border: 1px solid #e2e8f0;
  transition: all 0.3s;
}

.custom-dialog-input :deep(.el-input__wrapper.is-focus) {
  background-color: #ffffff;
  border-color: #3b82f6;
  box-shadow: 0 0 0 1px #3b82f6;
}

.custom-checkbox :deep(.el-checkbox__label) {
  padding-left: 6px;
}

.custom-checkbox :deep(.el-checkbox__inner) {
  border-radius: 4px;
}

.custom-checkbox :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #3b82f6;
  border-color: #3b82f6;
}

.custom-checkbox :deep(.el-checkbox__input.is-focus .el-checkbox__inner) {
  border-color: #3b82f6;
}

.animate-spin-slow {
  animation: spin 3s linear infinite;
}

/* 过渡动画 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.2s ease-in-out;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(10px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-10px);
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
