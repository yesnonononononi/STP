<template>
  <div class="w-full animate-fade-in">
    <el-form ref="forgetFormRef" :model="forgetForm" :rules="forgetRules" label-width="0" class="space-y-4">
      <el-form-item prop="phoneNumber">
        <el-input 
          v-model="forgetForm.phoneNumber" 
          placeholder="请输入绑定的手机号" 
          :prefix-icon="Cellphone" 
          class="custom-input"
        />
      </el-form-item>
      
      <el-form-item prop="verifyCode" class="!mb-4">
        <div class="flex w-full gap-3">
          <el-input 
            v-model="forgetForm.verifyCode" 
            placeholder="请输入短信验证码" 
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
      
      <el-form-item prop="newPassword">
        <el-input 
          v-model="forgetForm.newPassword" 
          type="password" 
          placeholder="请输入新密码" 
          :prefix-icon="Lock" 
          show-password
          class="custom-input"
        />
      </el-form-item>
      
      <el-form-item prop="confirmPassword">
        <el-input 
          v-model="forgetForm.confirmPassword" 
          type="password" 
          placeholder="请确认新密码" 
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
          @click="handleReset"
        >
          重置密码
        </el-button>
        
        <div class="flex justify-center items-center text-sm mt-2">
          <router-link class="text-cyan-400 hover:text-cyan-300 font-medium transition-colors" to="/auth/login">返回登录</router-link>
        </div>
      </div>
    </el-form>
  </div>
</template>

<script lang="ts" setup>
import { reactive, ref, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Cellphone, Lock, Key } from '@element-plus/icons-vue'

const router = useRouter()
const forgetFormRef = ref<FormInstance>()
const loading = ref(false)
const codeCountdown = ref(0)
let timer: any = null

const forgetForm = reactive({
  phoneNumber: '',
  verifyCode: '',
  newPassword: '',
  confirmPassword: '',
})

const validateConfirmPassword = (rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入新密码'))
  } else if (value !== forgetForm.newPassword) {
    callback(new Error('两次输入新密码不一致!'))
  } else {
    callback()
  }
}

const forgetRules = reactive<FormRules>({
  phoneNumber: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  verifyCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
})

const sendVerifyCode = () => {
  if (!forgetForm.phoneNumber || !/^1[3-9]\d{9}$/.test(forgetForm.phoneNumber)) {
    ElMessage.warning('请先输入有效的手机号')
    return
  }
  
  ElMessage.success('重置验证码已发送（测试模式：123456）')
  codeCountdown.value = 60
  timer = setInterval(() => {
    if (codeCountdown.value > 0) {
      codeCountdown.value--
    } else {
      clearInterval(timer)
    }
  }, 1000)
}

const handleReset = async () => {
  if (!forgetFormRef.value) return
  await forgetFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      // 模拟重置网络请求
      setTimeout(() => {
        loading.value = false
        ElMessage.success('密码重置成功！请用新密码登录')
        router.push('/auth/login')
      }, 1500)
    }
  })
}

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style>
/* 输入框样式复用 registerPage 结构 */
</style>
