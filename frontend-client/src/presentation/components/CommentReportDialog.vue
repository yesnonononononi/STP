<template>
  <Teleport to="body">
    <div v-if="visible" class="fixed inset-0 z-1000 flex items-center justify-center bg-black/40 backdrop-blur-xs">
      <div class="w-115 h-auto bg-white flex flex-col gap-4 shadow-xl rounded-2xl p-5 border border-gray-100 animate-fade-in">
        <!-- 头部 -->
        <div class="text-lg font-bold flex items-center justify-between pb-2 border-b border-gray-100">
          <div class="flex items-center gap-2">
            <svg class="size-5 text-red-500" viewBox="0 0 1024 1024" fill="currentColor">
              <path d="M512 64a448 448 0 1 1 0 896 448 448 0 0 1 0-896z m0 704a48 48 0 1 0 0-96 48 48 0 0 0 0 96z m0-480a48 48 0 0 0-48 48v224a48 48 0 0 0 96 0V336a48 48 0 0 0-48-48z"/>
            </svg>
            <span>举报评论</span>
          </div>
          <svg @click="handleClose"
            class="icon size-5 text-gray-400 cursor-pointer hover:text-gray-600 transition-colors"
            viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg">
            <path d="M1024 930.133333L930.133333 1024 512 605.866667 93.866667 1024 0 930.133333 418.133333 512 0 93.866667 93.866667 0 512 418.133333 930.133333 0 1024 93.866667 605.866667 512z"
              fill="currentColor"></path>
          </svg>
        </div>

        <!-- 被举报评论简要预览 -->
        <div v-if="commentContent" class="bg-slate-50 p-3 rounded-xl border border-slate-100 text-xs text-gray-600 space-y-1">
          <span class="font-medium text-gray-500 block">被举报评论：</span>
          <p class="line-clamp-2 text-gray-700 font-normal italic">“{{ commentContent }}”</p>
        </div>

        <!-- 快捷标签选择 -->
        <div class="flex flex-col gap-2">
          <span class="text-xs font-medium text-gray-600">举报原因类型：</span>
          <div class="flex flex-wrap gap-2">
            <button
              v-for="tag in presetReasons"
              :key="tag"
              type="button"
              @click="selectPresetReason(tag)"
              class="px-2.5 py-1 text-xs rounded-full border transition-all cursor-pointer"
              :class="reason === tag ? 'bg-red-50 text-red-600 border-red-200 font-medium shadow-xs' : 'bg-gray-50 text-gray-600 border-gray-200 hover:bg-gray-100'"
            >
              {{ tag }}
            </button>
          </div>
        </div>

        <!-- 详细原因输入 -->
        <div class="flex flex-col gap-2">
          <span class="text-xs font-medium text-gray-600">详细描述：</span>
          <textarea
            v-model="reason"
            class="w-full h-24 rounded-xl border-gray-200 border p-3 resize-none text-sm text-gray-700 placeholder-gray-400 focus:outline-hidden focus:border-red-400 focus:ring-1 focus:ring-red-100 transition-all"
            placeholder="请填写具体的违规举报原因..."
          ></textarea>
        </div>

        <!-- 底部控制按钮 -->
        <div class="flex justify-end items-center gap-3 pt-2">
          <el-button @click="handleClose" class="rounded-lg">取消</el-button>
          <el-button type="danger" :loading="loading" @click="handleSubmit" class="rounded-lg">提交举报</el-button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script lang="ts" setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { CommentAPI } from '@/services/comment/api'

const props = defineProps<{
  visible: boolean
  commentId: string | number
  commentContent?: string
}>()

const emit = defineEmits<{
  (e: 'update:visible', val: boolean): void
  (e: 'close'): void
  (e: 'success'): void
}>()

const reason = ref('')
const loading = ref(false)

const presetReasons = ['垃圾广告', '人身攻击/辱骂', '违法违规内容', '色情低俗', '涉嫌诈骗', '其他原因']

function selectPresetReason(tag: string) {
  reason.value = tag
}

function handleClose() {
  reason.value = ''
  emit('update:visible', false)
  emit('close')
}

async function handleSubmit() {
  if (!reason.value.trim()) {
    ElMessage.warning('请输入或选择举报原因')
    return
  }

  loading.value = true
  try {
    const res = await CommentAPI.report(props.commentId, reason.value.trim())
    if (res.code === 1) {
      ElMessage.success('举报已提交，我们会尽快核实处理')
      reason.value = ''
      emit('success')
      handleClose()
    } else {
      ElMessage.error(res.errMsg || '举报提交失败')
    }
  } catch (e: any) {
    ElMessage.error(e.message || '举报提交失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

watch(() => props.visible, (val) => {
  if (!val) {
    reason.value = ''
  }
})
</script>
