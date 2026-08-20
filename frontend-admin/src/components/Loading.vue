<template>
  <div v-if="isVisible" class="absolute inset-0 z-50 flex items-center justify-center pointer-events-auto transition-all" :class="props.bgColor">
    <div class="flex flex-col items-center justify-center p-4 gap-2 w-auto overflow-hidden">
      <div v-if="display" v-html="display" class="overflow-hidden"></div>
      <div v-else class="loading-text">STP</div>
      <span v-if="prompt" class="animate-pulse text-slate-500 text-xs font-medium tracking-wide mt-1">{{ prompt }}</span>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue'

const visible = defineModel<boolean>()
const props = withDefaults(
  defineProps<{
    bgColor?: string
    prompt?: string
    display?: string
  }>(),
  {
    bgColor: 'bg-white/80 backdrop-blur-xs',
    prompt: '',
    display: '<div class="loading-text" data-text="STP">STP</div>'
  }
)

const isVisible = computed(() => (visible.value === undefined ? true : visible.value))
</script>

<style scoped>
:deep(.loading-text),
.loading-text {
  font-size: 32px;
  font-weight: 800;
  letter-spacing: 2px;
  position: relative;
  display: inline-block;
  overflow: hidden;

  /* 基础灰色背景，叠加宽度不超过 1/3 文字长的蓝色高光画布 */
  background-image: 
    linear-gradient(
      90deg, 
      transparent 0%, 
      rgba(59, 130, 246, 0.4) 15%, 
      #2563eb 50%, 
      rgba(59, 130, 246, 0.4) 85%, 
      transparent 100%
    ),
    linear-gradient(#94a3b8, #94a3b8);

  /* 蓝色画布宽度限定为 30% */
  background-size: 30% 100%, 100% 100%;
  background-repeat: no-repeat, no-repeat;

  /* overflow-hidden 裁切 */
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;

  /* 平滑划过扫光动画 */
  animation: blueCanvasSweep 1.4s cubic-bezier(0.4, 0, 0.2, 1) infinite;
}

@keyframes blueCanvasSweep {
  0% {
    background-position: -35% 0, 0 0;
  }
  100% {
    background-position: 135% 0, 0 0;
  }
}
</style>
