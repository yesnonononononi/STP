<template>
  <div ref="triggerRef" class="inline-flex items-center select-none" @mouseenter="show" @mouseleave="hide">
    <!-- 触发器插槽 -->
    <div class="inline-flex items-center cursor-pointer">
      <slot></slot>
    </div>

    <!-- 将提示框通过 Teleport 挂载到 body 最外层，彻底不受父级 overflow: hidden 和层叠上下文的影响 -->
    <Teleport to="body">
      <Transition
        enter-active-class="transition-all duration-200 ease-out"
        enter-from-class="opacity-0 scale-95"
        enter-to-class="opacity-100 scale-100"
        leave-active-class="transition-all duration-150 ease-in"
        leave-from-class="opacity-100 scale-100"
        leave-to-class="opacity-0 scale-95"
      >
        <div
          v-if="isVisible"
          ref="floatingRef"
          class="fixed z-[99999] pointer-events-none drop-shadow-2xl"
          :style="floatingStyle"
        >
          <div
            class="px-3 py-2 text-xs rounded-lg shadow-2xl border relative font-normal leading-relaxed text-left max-w-xs w-max"
            :class="themeClasses[theme]"
          >
            <slot name="content">{{ content }}</slot>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick } from 'vue'

interface Props {
  content?: string;
  placement?: 'top' | 'bottom' | 'left' | 'right';
  theme?: 'dark' | 'light' | 'glass';
  width?: string;
}

const props = withDefaults(defineProps<Props>(), {
  content: '',
  placement: 'top',
  theme: 'dark',
  width: 'max-w-xs w-max'
});

const isVisible = ref(false)
const triggerRef = ref<HTMLElement | null>(null)
const floatingRef = ref<HTMLElement | null>(null)
const coords = ref({ top: 0, left: 0 })

// 主题映射类
const themeClasses = {
  dark: 'bg-slate-900 text-white border-slate-800 shadow-slate-950/60',
  light: 'bg-white text-slate-800 border-slate-200 shadow-2xl',
  glass: 'bg-slate-900/95 border-slate-800 text-white shadow-2xl backdrop-blur-xl'
};

const floatingStyle = computed(() => ({
  top: `${coords.value.top}px`,
  left: `${coords.value.left}px`
}))

const updatePosition = () => {
  if (!triggerRef.value) return
  const rect = triggerRef.value.getBoundingClientRect()
  const offset = 8

  // 默认位置计算
  let top = rect.top - offset
  let left = rect.left + rect.width / 2

  if (floatingRef.value) {
    const floatRect = floatingRef.value.getBoundingClientRect()
    if (props.placement === 'top') {
      top = rect.top - floatRect.height - offset
    } else if (props.placement === 'bottom') {
      top = rect.bottom + offset
    }
    left = rect.left + rect.width / 2 - floatRect.width / 2
  }

  coords.value = { top, left }
}

const show = async () => {
  isVisible.value = true
  await nextTick()
  updatePosition()
}

const hide = () => {
  isVisible.value = false
}
</script>
