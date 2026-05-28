<template>
  <div class="w-full animate-fade-in">
    <el-form
      ref="registerFormRef"
      :model="registerForm"
      :rules="registerRules"
      label-width="0"
      class="space-y-4"
    >
      <el-form-item prop="phoneNumber">
        <el-input
          v-model="registerForm.phoneNumber"
          placeholder="请输入手机号"
          :prefix-icon="Cellphone"
          class="custom-input"
        />
      </el-form-item>

      <el-form-item prop="verifyCode" class="!mb-4">
        <div class="flex w-full gap-3">
          <el-input
            v-model="registerForm.verifyCode"
            placeholder="请输入验证码"
            :prefix-icon="Key"
            class="custom-input flex-1"
          />
          <el-button
            type="info"
            plain
            class="h-10 rounded-lg shrink-0 px-4 glass-light text-slate-200 border-white/10 hover:bg-white/20 active:scale-[0.98] transition-all"
            :disabled="codeCountdown > 0"
            @click="sendVerifyCode"
          >
            {{ codeCountdown > 0 ? `${codeCountdown}s` : '获取验证码' }}
          </el-button>
        </div>
      </el-form-item>

      <el-form-item prop="password">
        <el-input
          v-model="registerForm.password"
          type="password"
          placeholder="请输入密码"
          :prefix-icon="Lock"
          show-password
          class="custom-input"
        />
      </el-form-item>

      <el-form-item prop="confirmPassword">
        <el-input
          v-model="registerForm.confirmPassword"
          type="password"
          placeholder="请确认密码"
          :prefix-icon="Lock"
          show-password
          class="custom-input"
        />
      </el-form-item>

      <div class="flex flex-col gap-4 mt-6">
        <el-button
          type="primary"
          class="w-full h-10 rounded-lg text-base font-semibold bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-600 hover:to-blue-700 border-none transition-all duration-300 transform active:scale-[0.98]"
          :loading="loading"
          @click="handleRegister"
        >
          注册
        </el-button>

        <div class="flex justify-center items-center text-sm mt-2">
          <span class="text-slate-400">已有账号？</span>
          <router-link
            class="text-cyan-400 hover:text-cyan-300 font-medium transition-colors"
            to="/auth/login"
            >立即登录</router-link
          >
        </div>
      </div>
    </el-form>
  </div>
</template>

<script lang="ts" setup>
import { reactive, ref, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { type FormInstance, type FormRules } from 'element-plus'
import { Cellphone, Lock, Key } from '@element-plus/icons-vue'
import { Auther } from '@/views/auth/composables/Auth'
import { log } from '@/utils/log'

const router = useRouter()
const auther = Auther.getInstance()
const registerFormRef = ref<FormInstance>()
const loading = ref(false)
const codeCountdown = ref(0)
let timer: any = null

const registerForm = reactive({
  phoneNumber: '',
  verifyCode: '',
  password: '',
  confirmPassword: '',
})

// 验证两次密码是否一致
const validateConfirmPassword = (rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

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
  confirmPassword: [{ required: true, validator: validateConfirmPassword, trigger: 'blur' }],
})

const sendVerifyCode = () => {
  if (!registerForm.phoneNumber || !/^1[3-9]\d{9}$/.test(registerForm.phoneNumber)) {
    log.warning('请先输入有效的手机号')
    return
  }

  // 模拟发送验证码，实际可调用API
  log.success('验证码已发送（测试模式：123456）')
  codeCountdown.value = 60
  timer = setInterval(() => {
    if (codeCountdown.value > 0) {
      codeCountdown.value--
    } else {
      clearInterval(timer)
    }
  }, 1000)
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
        log.success('注册成功！正在跳转至登录页面...')
        setTimeout(() => {
          router.push('/auth/login')
        }, 1500)
      } catch (err: any) {
        console.error(err)
      } finally {
        loading.value = false
      }
    }
  })
}

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style>
/* 深度自定义输入框样式以融合毛玻璃暗色主题 */
.custom-input .el-input__wrapper {
  background-color: rgba(255, 255, 255, 0.05) !important;
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.1) inset !important;
  border-radius: 8px !important;
  height: 40px !important;
  transition: all 0.3s ease !important;
}

.custom-input .el-input__wrapper.is-focus,
.custom-input .el-input__wrapper:hover {
  box-shadow: 0 0 0 1px rgba(34, 211, 238, 0.5) inset !important;
  background-color: rgba(255, 255, 255, 0.08) !important;
}

.custom-input .el-input__inner {
  color: #f1f5f9 !important;
}

.custom-input .el-input__inner::placeholder {
  color: #94a3b8 !important;
}

.custom-input .el-input__prefix-inner {
  color: #94a3b8 !important;
}
</style>
