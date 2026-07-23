<template>
  <div v-if="hasVipInfo" class="inline-flex items-center gap-1.5 select-none shrink-0 align-middle">
    <!-- 1. 会员等级 Tag (蓝色毛玻璃渐变) -->
    <div
      v-if="displayLevelName"
      class="inline-flex items-center gap-1 px-1.5 py-0.5 rounded-md text-[10px] md:text-xs bg-gradient-to-r from-blue-50 to-indigo-50 border border-blue-100 text-blue-600 hover:scale-105 transition-transform duration-200"
    >
      <img
        v-if="displayIcon"
        class="w-3.5 h-3.5 object-contain"
        :src="displayIcon"
        alt="vip-icon"
      />
      <span class="font-medium">{{ displayLevelName }}</span>
    </div>

    <!-- 2. 会员身份 Tag (金黄色微光渐变) -->
    <span
      v-if="displayVipType"
      class="inline-flex items-center justify-center px-1.5 py-0.5 rounded-md text-[10px] md:text-xs bg-gradient-to-r from-amber-400 via-yellow-400 to-amber-500 text-white font-bold shadow-xs hover:brightness-105 hover:scale-105 transition-all duration-200"
    >
      {{ displayVipType }}
    </span>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface UserLike {
  vipConfigIcon?: string
  memberLevelName?: string
  vipType?: string
}

const props = defineProps<{
  user?: UserLike | null
  vipConfigIcon?: string
  memberLevelName?: string
  vipType?: string
}>()

const displayIcon = computed(() => props.vipConfigIcon || props.user?.vipConfigIcon || '')

const displayLevelName = computed(() => {
  const name = props.memberLevelName || props.user?.memberLevelName || ''
  if (name.includes('普通') || name === '无' || name === '普通用户') {
    return ''
  }
  return name
})

const displayVipType = computed(() => {
  const type = props.vipType || props.user?.vipType || ''
  if (type.includes('普通') || type === '无' || type === '普通用户') {
    return ''
  }
  return type
})

const hasVipInfo = computed(() => {
  return !!(displayLevelName.value || displayVipType.value)
})
</script>
