<template>
  <div
    class="min-h-screen flex items-center justify-center bg-linear-to-br from-slate-900 via-indigo-950 to-slate-900 p-4 relative overflow-hidden"
  >
    <!-- 背景光效装饰物 -->
    <div class="absolute -top-40 -left-40 w-96 h-96 bg-blue-500/10 rounded-full blur-3xl"></div>
    <div
      class="absolute -bottom-40 -right-40 w-96 h-96 bg-purple-500/10 rounded-full blur-3xl"
    ></div>

    <!-- 登录注册主体卡片 - 毛玻璃效果 -->
    <div
      class="w-full max-w-110 glass p-8 rounded-2xl shadow-2xl relative border border-white/10 flex flex-col"
    >
      <!-- 头部 Logo 与 标题 -->
      <div class="flex flex-col items-center mb-8">
        <div
          class="h-16 w-16 bg-white/10 rounded-2xl flex items-center justify-center mb-3 shadow-inner border border-white/10 transition-transform duration-300 hover:scale-105"
        >
          <svg
            class="h-10 w-10 text-cyan-400"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"
            />
          </svg>
        </div>
        <h2
          class="text-3xl font-extrabold text-transparent bg-clip-text bg-linear-to-r from-white to-cyan-300 tracking-wider"
        >
          STP Portal
        </h2>
        <p class="text-slate-400 text-sm mt-2">安全、快捷、高效的数字服务平台</p>
      </div>

      <!-- 子路由视口，渲染具体的登录、注册、忘记密码视图 -->
      <router-view v-slot="{ Component }">
        <transition name="fade-slide" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </div>
  </div>
</template>

<script lang="ts" setup>
// 统一的 Auth 模板组件，内部通过 router-view 切换子组件
</script>

<style scoped>
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(12px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-12px);
}
</style>
