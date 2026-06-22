<template>
  <Teleport to="body">
    <div v-if="visible" class="fixed inset-0 z-50 flex items-center justify-center bg-black/40" @click="emit('close')">
      <div class="w-[420px] bg-white rounded-xl shadow-xl overflow-hidden border border-gray-100 flex flex-col" @click.stop>
        <!-- Header -->
        <div class="p-4 px-6 border-b border-gray-100 flex justify-between items-center bg-linear-to-r from-blue-50 to-blue-100 select-none">
          <span class="font-bold text-gray-800">帖子可见性设置</span>
          <button @click="emit('close')" class="text-gray-400 hover:text-gray-600 text-2xl font-light leading-none cursor-pointer">×</button>
        </div>
        <!-- Options -->
        <div class="p-4 px-6 flex flex-col gap-4 bg-white">
          <!-- Option: Public -->
          <div class="flex items-center justify-between cursor-pointer p-3 rounded-xl hover:bg-blue-50/40 transition-all duration-200 select-none"
               @click="handleScopeChange(1)">
            <div class="flex flex-col flex-1 pr-2">
              <span class="text-sm font-semibold text-gray-700">公开</span>
              <span class="text-xs text-gray-400 mt-1.5 leading-relaxed">公开的帖子，所有同学和游客都可以直接阅读</span>
            </div>
            <div class="w-12 h-6 rounded-xl transition-all duration-300 flex items-center shrink-0 p-0.5"
                 :class="Number(selectedScope) === 1 ? 'bg-linear-to-r from-blue-400 to-blue-500' : 'bg-gray-200'">
              <div class="w-5 h-5 rounded-full bg-white shadow-sm transition-all duration-300"
                   :class="Number(selectedScope) === 1 ? 'translate-x-6' : 'translate-x-0'">
              </div>
            </div>
          </div>

          <!-- Option: Private -->
          <div class="flex items-center justify-between cursor-pointer p-3 rounded-xl hover:bg-blue-50/40 transition-all duration-200 select-none"
               @click="handleScopeChange(2)">
            <div class="flex flex-col flex-1 pr-2">
              <span class="text-sm font-semibold text-gray-700">私密</span>
              <span class="text-xs text-gray-400 mt-1.5 leading-relaxed">私密的帖子，仅发布者本人可以检索和阅读</span>
            </div>
            <div class="w-12 h-6 rounded-xl transition-all duration-300 flex items-center shrink-0 p-0.5"
                 :class="Number(selectedScope) === 2 ? 'bg-linear-to-r from-blue-400 to-blue-500' : 'bg-gray-200'">
              <div class="w-5 h-5 rounded-full bg-white shadow-sm transition-all duration-300"
                   :class="Number(selectedScope) === 2 ? 'translate-x-6' : 'translate-x-0'">
              </div>
            </div>
          </div>

          <!-- Option: Protected (Friends only) -->
          <div class="flex items-center justify-between cursor-pointer p-3 rounded-xl hover:bg-blue-50/40 transition-all duration-200 select-none"
               @click="handleScopeChange(3)">
            <div class="flex flex-col flex-1 pr-2">
              <span class="text-sm font-semibold text-gray-700">好友可见</span>
              <span class="text-xs text-gray-400 mt-1.5 leading-relaxed">仅限互相关注（好友关系）的同学可以直接阅读</span>
            </div>
            <div class="w-12 h-6 rounded-xl transition-all duration-300 flex items-center shrink-0 p-0.5"
                 :class="Number(selectedScope) === 3 ? 'bg-linear-to-r from-blue-400 to-blue-500' : 'bg-gray-200'">
              <div class="w-5 h-5 rounded-full bg-white shadow-sm transition-all duration-300"
                   :class="Number(selectedScope) === 3 ? 'translate-x-6' : 'translate-x-0'">
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script lang="ts" setup>
import { ref, watch } from 'vue'
import { PostAPI, type PostVO } from '@/services/post'
import { log } from '@/utils/log'

const props = defineProps<{
  visible: boolean
  post: PostVO
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'update-scope', scope: number): void
}>()

// 本地状态，确保 UI 响应瞬间完成
const selectedScope = ref(1)

watch(
  () => props.post?.visibleScope,
  (newVal) => {
    if (newVal !== undefined && newVal !== null) {
      selectedScope.value = Number(newVal)
    }
  },
  { immediate: true }
)

const handleScopeChange = async (scope: number) => {
  if (!props.post) return
  const oldScope = selectedScope.value

  // 1. 乐观更新（Optimistic Update），本地状态秒切，滑块瞬发响应
  selectedScope.value = scope
  emit('update-scope', scope)

  // 2. 如果存在 post.id，则在后台异步修改数据库
  if (props.post.id) {
    try {
      const res = await PostAPI.setVisible(props.post.id.toString(), scope)
      if (res.code !== 1) {
        log.error(res.errMsg || '修改失败')
        // 发生错误时回滚状态
        selectedScope.value = oldScope
        emit('update-scope', oldScope)
      }
    } catch (err: any) {
      log.error(err.message || '修改失败')
      // 发生错误时回滚状态
      selectedScope.value = oldScope
      emit('update-scope', oldScope)
    }
  }
}
</script>
