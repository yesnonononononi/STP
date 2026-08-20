<template>
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="modelValue" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-xs">
        <div class="w-full max-w-md bg-white rounded-2xl shadow-2xl overflow-hidden border border-slate-100 transform transition-all">
          <!-- 弹窗 Header -->
          <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
            <div class="flex items-center gap-2">
              <span class="w-2 h-2 rounded-full bg-rose-500 animate-ping"></span>
              <h3 class="text-base font-bold text-slate-800">举报该用户</h3>
            </div>
            <button @click="close" class="text-slate-400 hover:text-slate-600 transition-colors">
              <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>

          <!-- 弹窗 Body -->
          <div class="p-6 space-y-4">
            <p class="text-xs text-slate-500">
              举报对象：<strong class="text-slate-800 font-semibold">{{ targetNick || '用户 ID: ' + reportedId }}</strong>
            </p>

            <div class="space-y-2">
              <label class="block text-xs font-semibold text-slate-700">请选择常见举报类型</label>
              <div class="flex flex-wrap gap-2">
                <button
                  v-for="tag in quickTags"
                  :key="tag"
                  type="button"
                  @click="selectTag(tag)"
                  class="px-2.5 py-1 text-xs rounded-lg border transition-all"
                  :class="reason === tag ? 'border-rose-500 bg-rose-50 text-rose-600 font-medium' : 'border-slate-200 text-slate-600 hover:border-slate-300'"
                >
                  {{ tag }}
                </button>
              </div>
            </div>

            <div class="space-y-1.5">
              <label class="block text-xs font-semibold text-slate-700">具体举报原因说明</label>
              <textarea
                v-model="reason"
                rows="3"
                placeholder="请输入具体的违规行为说明（如垃圾广告、骚扰发帖、违法言论等）..."
                class="w-full text-xs p-3 rounded-xl border border-slate-200 focus:border-rose-500 focus:ring-1 focus:ring-rose-500 outline-hidden transition-all text-slate-800 placeholder:text-slate-400"
              ></textarea>
            </div>
          </div>

          <!-- 弹窗 Footer -->
          <div class="px-6 py-3.5 bg-slate-50 border-t border-slate-100 flex items-center justify-end gap-2">
            <button
              @click="close"
              class="px-4 py-2 rounded-xl text-xs text-slate-600 hover:bg-slate-200/60 font-medium transition-all"
            >
              取消
            </button>
            <button
              @click="submitReport"
              :disabled="submitting || !reason.trim()"
              class="px-4 py-2 rounded-xl text-xs bg-rose-500 hover:bg-rose-600 text-white font-medium shadow-md shadow-rose-500/20 active:scale-95 disabled:opacity-50 transition-all flex items-center gap-1.5"
            >
              <span v-if="submitting" class="w-3.5 h-3.5 border-2 border-white/30 border-t-white rounded-full animate-spin"></span>
              <span>提交举报</span>
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { UserAPI } from '@/services/user'

const props = defineProps<{
  modelValue: boolean
  reportedId: string | number
  targetNick?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}>()

const quickTags = ['垃圾广告营销', '人身攻击谩骂', '发布违法违规', '色情低俗内容', '冒充他人账号']
const reason = ref('')
const submitting = ref(false)

const selectTag = (tag: string) => {
  reason.value = tag
}

const close = () => {
  reason.value = ''
  emit('update:modelValue', false)
}

const submitReport = async () => {
  if (!reason.value.trim()) return
  submitting.value = true
  try {
    const res = await UserAPI.reportUser(props.reportedId, reason.value.trim())
    if (res.code === 1) {
      alert('举报成功，社区管理员将尽快进行审查处理！')
      emit('success')
      close()
    } else {
      alert(res.errMsg || '提交举报失败')
    }
  } catch (err: any) {
    alert(err?.response?.data?.errMsg || '举报提交失败，请重试')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
