<template>
  <div class="space-y-6 animate-fade-in text-slate-800 max-w-4xl">
    <div class="p-8 rounded-xl border border-slate-200 bg-white shadow-xs flex flex-col items-center text-center gap-2">
      <el-icon class="text-3xl text-slate-300"><Warning /></el-icon>
      <h3 class="text-lg font-semibold text-slate-800">会员系统全局配置</h3>
      <p class="text-sm text-slate-500 max-w-xl">
        该功能依赖的后端接口（<code class="font-mono">/a/member/config</code>）尚未实现，相关入口已下线。
        会员体系请通过「等级配置」「套餐配置」「用户会员记录」进行管理。
      </p>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { Warning } from '@element-plus/icons-vue'
// 占位页：后端未提供 /a/member/config、/a/member/config/update 接口，已移除幽灵接口调用。
</script>
