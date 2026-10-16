<template>
  <div class="space-y-6 animate-fade-in text-slate-800">
    <div class="p-8 rounded-xl border border-slate-200 bg-white shadow-xs flex flex-col items-center text-center gap-2">
      <el-icon class="text-3xl text-slate-300"><Warning /></el-icon>
      <h3 class="text-lg font-semibold text-slate-800">会员个性装饰</h3>
      <p class="text-sm text-slate-500 max-w-xl">
        该功能依赖的后端接口（<code class="font-mono">/a/member/decoration/*</code>）尚未实现，相关入口已下线。
      </p>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { Warning } from '@element-plus/icons-vue'
// 占位页：后端未提供 /a/member/decoration/list|save|toggle-status|delete 接口，已移除幽灵接口调用。
</script>
