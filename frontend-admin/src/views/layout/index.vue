<template>
  <div class="min-h-screen flex bg-slate-50 text-slate-800 font-sans">
    <!-- 侧边栏 -->
    <aside 
      class="bg-white border-r border-slate-200/80 flex flex-col transition-all duration-300 relative z-20 shrink-0 select-none"
      :class="isCollapsed ? 'w-20' : 'w-64'"
    >
      <!-- Logo 区域 -->
      <div class="h-16 flex items-center justify-center border-b border-slate-100 px-4 gap-3 overflow-hidden">
        <div class="w-8 h-8 rounded-lg bg-gradient-to-br from-blue-500 to-indigo-600 flex items-center justify-center shadow-md shadow-blue-500/10 shrink-0">
          <span class="font-extrabold text-white text-lg">S</span>
        </div>
        <span 
          v-show="!isCollapsed" 
          class="font-bold text-lg text-slate-800 transition-all duration-300 shrink-0"
        >
          STP 后台系统
        </span>
      </div>

      <!-- 导航菜单 -->
      <nav class="flex-1 py-4 px-3 space-y-1.5 overflow-y-auto">
        <!-- 1. 管理员管理 -->
        <router-link
          to="/admin"
          class="flex items-center gap-3 py-2.5 rounded-lg text-slate-600 hover:bg-slate-50 hover:text-slate-900 transition-all duration-200 group"
          :class="isCollapsed ? 'justify-center px-0' : 'px-3.5'"
          active-class="bg-blue-50/60 text-blue-600 border border-blue-100/80 font-medium"
        >
          <el-icon class="text-lg group-hover:text-blue-600 transition-colors shrink-0">
            <User />
          </el-icon>
          <span v-show="!isCollapsed" class="text-sm transition-all duration-300">管理员管理</span>
        </router-link>

        <!-- 2. 用户管理 -->
        <router-link
          to="/user"
          class="flex items-center gap-3 py-2.5 rounded-lg text-slate-600 hover:bg-slate-50 hover:text-slate-900 transition-all duration-200 group"
          :class="isCollapsed ? 'justify-center px-0' : 'px-3.5'"
          active-class="bg-blue-50/60 text-blue-600 border border-blue-100/80 font-medium"
        >
          <el-icon class="text-lg group-hover:text-blue-600 transition-colors shrink-0">
            <UserFilled />
          </el-icon>
          <span v-show="!isCollapsed" class="text-sm transition-all duration-300">用户管理</span>
        </router-link>

        <!-- 3. 会员管理 (下拉框) -->
        <div>
          <button
            @click="toggleSubMenu('member')"
            class="w-full flex items-center py-2.5 rounded-lg text-slate-600 hover:bg-slate-50 hover:text-slate-900 transition-all duration-200 group"
            :class="[
              isCollapsed ? 'justify-center px-0' : 'justify-between px-3.5',
              isPathActive('/member') ? 'text-blue-600 font-medium bg-blue-50/30' : ''
            ]"
          >
            <div class="flex items-center gap-3">
              <el-icon class="text-lg group-hover:text-blue-600 transition-colors shrink-0">
                <Medal />
              </el-icon>
              <span v-show="!isCollapsed" class="text-sm">会员管理</span>
            </div>
            <el-icon v-show="!isCollapsed" class="text-xs transition-transform duration-200" :class="subMenuOpen.member ? 'rotate-180' : ''">
              <ArrowDown />
            </el-icon>
          </button>

          <!-- 会员管理 子菜单 -->
          <transition
            enter-active-class="transition-all duration-300 ease-out overflow-hidden"
            enter-from-class="max-h-0 opacity-0 transform -translate-y-1"
            enter-to-class="max-h-48 opacity-100 transform translate-y-0"
            leave-active-class="transition-all duration-200 ease-in overflow-hidden"
            leave-from-class="max-h-48 opacity-100 transform translate-y-0"
            leave-to-class="max-h-0 opacity-0 transform -translate-y-1"
          >
            <div v-show="subMenuOpen.member && !isCollapsed" class="ml-7 mt-1 space-y-1 border-l-2 border-slate-100 pl-3">
              <router-link
                to="/member/level"
                class="block px-3 py-1.5 rounded-md text-xs text-slate-500 hover:text-blue-600 hover:bg-slate-50 transition-colors"
                active-class="text-blue-600 font-medium bg-blue-50/50"
              >
                等级配置
              </router-link>
              <router-link
                to="/member/package"
                class="block px-3 py-1.5 rounded-md text-xs text-slate-500 hover:text-blue-600 hover:bg-slate-50 transition-colors"
                active-class="text-blue-600 font-medium bg-blue-50/50"
              >
                套餐配置
              </router-link>
              <router-link
                to="/member/user"
                class="block px-3 py-1.5 rounded-md text-xs text-slate-500 hover:text-blue-600 hover:bg-slate-50 transition-colors"
                active-class="text-blue-600 font-medium bg-blue-50/50"
              >
                用户会员记录
              </router-link>
            </div>
          </transition>
        </div>

        <!-- 4. 优惠券管理 (下拉框) -->
        <div>
          <button
            @click="toggleSubMenu('coupon')"
            class="w-full flex items-center py-2.5 rounded-lg text-slate-600 hover:bg-slate-50 hover:text-slate-900 transition-all duration-200 group"
            :class="[
              isCollapsed ? 'justify-center px-0' : 'justify-between px-3.5',
              isPathActive('/coupon') ? 'text-blue-600 font-medium bg-blue-50/30' : ''
            ]"
          >
            <div class="flex items-center gap-3">
              <el-icon class="text-lg group-hover:text-blue-600 transition-colors shrink-0">
                <Ticket />
              </el-icon>
              <span v-show="!isCollapsed" class="text-sm">优惠券</span>
            </div>
            <el-icon v-show="!isCollapsed" class="text-xs transition-transform duration-300" :class="subMenuOpen.coupon ? 'rotate-180' : ''">
              <ArrowDown />
            </el-icon>
          </button>

          <!-- 优惠券 子菜单 -->
          <transition
            enter-active-class="transition-all duration-300 ease-out overflow-hidden"
            enter-from-class="max-h-0 opacity-0 transform -translate-y-1"
            enter-to-class="max-h-48 opacity-100 transform translate-y-0"
            leave-active-class="transition-all duration-200 ease-in overflow-hidden"
            leave-from-class="max-h-48 opacity-100 transform translate-y-0"
            leave-to-class="max-h-0 opacity-0 transform -translate-y-1"
          >
            <div v-show="subMenuOpen.coupon && !isCollapsed" class="ml-7 mt-1 space-y-1 border-l-2 border-slate-100 pl-3">
              <router-link
                to="/coupon/manage"
                class="block px-3 py-1.5 rounded-md text-xs text-slate-500 hover:text-blue-600 hover:bg-slate-50 transition-colors"
                active-class="text-blue-600 font-medium bg-blue-50/50"
              >
                优惠券管理
              </router-link>
              <router-link
                to="/coupon/activity"
                class="block px-3 py-1.5 rounded-md text-xs text-slate-500 hover:text-blue-600 hover:bg-slate-50 transition-colors"
                active-class="text-blue-600 font-medium bg-blue-50/50"
              >
                活动管理
              </router-link>
            </div>
          </transition>
        </div>

        <!-- 5. 帖子管理 -->
        <router-link
          to="/post"
          class="flex items-center gap-3 py-2.5 rounded-lg text-slate-600 hover:bg-slate-50 hover:text-slate-900 transition-all duration-200 group"
          :class="isCollapsed ? 'justify-center px-0' : 'px-3.5'"
          active-class="bg-blue-50/60 text-blue-600 border border-blue-100/80 font-medium"
        >
          <el-icon class="text-lg group-hover:text-blue-600 transition-colors shrink-0">
            <Document />
          </el-icon>
          <span v-show="!isCollapsed" class="text-sm">帖子管理</span>
        </router-link>

        <!-- 6. 评论管理 -->
        <router-link
          to="/comment"
          class="flex items-center gap-3 py-2.5 rounded-lg text-slate-600 hover:bg-slate-50 hover:text-slate-900 transition-all duration-200 group"
          :class="isCollapsed ? 'justify-center px-0' : 'px-3.5'"
          active-class="bg-blue-50/60 text-blue-600 border border-blue-100/80 font-medium"
        >
          <el-icon class="text-lg group-hover:text-blue-600 transition-colors shrink-0">
            <ChatLineSquare />
          </el-icon>
          <span v-show="!isCollapsed" class="text-sm">评论管理</span>
        </router-link>

        <!-- 7. 订单管理 -->
        <router-link
          to="/order"
          class="flex items-center gap-3 py-2.5 rounded-lg text-slate-600 hover:bg-slate-50 hover:text-slate-900 transition-all duration-200 group"
          :class="isCollapsed ? 'justify-center px-0' : 'px-3.5'"
          active-class="bg-blue-50/60 text-blue-600 border border-blue-100/80 font-medium"
        >
          <el-icon class="text-lg group-hover:text-blue-600 transition-colors shrink-0">
            <ShoppingCart />
          </el-icon>
          <span v-show="!isCollapsed" class="text-sm">订单管理</span>
        </router-link>

        <!-- 8. 系统通知管理 -->
        <router-link
          to="/notification"
          class="flex items-center gap-3 py-2.5 rounded-lg text-slate-600 hover:bg-slate-50 hover:text-slate-900 transition-all duration-200 group"
          :class="isCollapsed ? 'justify-center px-0' : 'px-3.5'"
          active-class="bg-blue-50/60 text-blue-600 border border-blue-100/80 font-medium"
        >
          <el-icon class="text-lg group-hover:text-blue-600 transition-colors shrink-0">
            <Bell />
          </el-icon>
          <span v-show="!isCollapsed" class="text-sm">系统通知管理</span>
        </router-link>
      </nav>

      <!-- 侧边栏折叠按钮 -->
      <div class="p-4 border-t border-slate-100 flex justify-center">
        <button 
          @click="isCollapsed = !isCollapsed" 
          class="w-10 h-10 rounded-lg bg-slate-50 hover:bg-slate-100 text-slate-500 hover:text-slate-800 flex items-center justify-center transition-colors cursor-pointer border border-slate-200/50"
        >
          <el-icon class="text-lg">
            <Fold v-if="!isCollapsed" />
            <Expand v-else />
          </el-icon>
        </button>
      </div>
    </aside>

    <!-- 主体区域 -->
    <div class="flex-1 flex flex-col overflow-hidden relative">
      <!-- 顶部 Header -->
      <header class="h-16 bg-white/80 backdrop-blur-md border-b border-slate-100 px-6 flex items-center justify-between relative z-10">
        <!-- 左侧面包屑 -->
        <div class="flex items-center gap-3">
          <span class="text-sm text-slate-500">工作台</span>
          <span class="text-slate-300 font-light">/</span>
          <span class="text-sm text-slate-800 font-medium">{{ currentRouteTitle }}</span>
        </div>

        <!-- 右侧用户状态 -->
        <div class="flex items-center gap-4">
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="flex items-center gap-3 cursor-pointer group">
              <img 
                class="w-9 h-9 rounded-full border border-slate-200 group-hover:border-blue-500 transition-colors object-cover bg-slate-100" 
                :src="userProfile?.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" 
                alt="Avatar"
              />
              <span class="hidden sm:inline-block text-sm text-slate-600 group-hover:text-slate-800 transition-colors font-medium">
                {{ userProfile?.nick || '系统管理员' }}
              </span>
            </div>
            
            <template #dropdown>
              <el-dropdown-menu class="admin-dropdown-menu">
                <el-dropdown-item command="logout">
                  <div class="flex items-center gap-2 text-rose-500">
                    <el-icon><SwitchButton /></el-icon>
                    <span>退出登录</span>
                  </div>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 主视图区域 -->
      <main class="flex-1 overflow-y-auto p-6 md:p-8 bg-slate-50">
        <router-view></router-view>
      </main>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/views/auth/store'
import { useUserInfoStore } from '@/stores/userInfo'
import {
  User,
  UserFilled,
  Medal,
  Ticket,
  Document,
  ChatLineSquare,
  ShoppingCart,
  Bell,
  ArrowDown,
  Fold,
  Expand,
  SwitchButton
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const isCollapsed = ref(false)

const subMenuOpen = reactive({
  member: false,
  coupon: false
})

// 根据当前路由动态自动展开对应侧边栏下拉项
watch(
  () => route.path,
  (newPath) => {
    if (newPath.startsWith('/member')) {
      subMenuOpen.member = true
    } else if (newPath.startsWith('/coupon')) {
      subMenuOpen.coupon = true
    }
  },
  { immediate: true }
)

const toggleSubMenu = (menu: 'member' | 'coupon') => {
  subMenuOpen[menu] = !subMenuOpen[menu]
}

const isPathActive = (prefix: string) => {
  return route.path.startsWith(prefix)
}

const authStore = useAuthStore()
const userInfoStore = useUserInfoStore()

const userProfile = computed(() => userInfoStore.user)

const currentRouteTitle = computed(() => {
  return (route.meta?.title as string) || '工作台'
})

const handleCommand = (command: string) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出管理员后台系统吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      customClass: 'admin-msg-box'
    }).then(async () => {
      try {
        const { AuthAPI } = await import('@/services/auth')
        await AuthAPI.logout()
      } catch {
        // 异常静默
      } finally {
        authStore.clearAuth()
        userInfoStore.clearUser()
        router.push('/login')
      }
    }).catch(() => {})
  }
}
</script>

<style>
.admin-dropdown-menu {
  background-color: #ffffff !important;
  border: 1px solid #e2e8f0 !important;
  border-radius: 8px !important;
  padding: 4px !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05) !important;
}

.admin-dropdown-menu .el-dropdown-menu__item {
  color: #475569 !important;
  border-radius: 6px !important;
  padding: 8px 12px !important;
}

.admin-dropdown-menu .el-dropdown-menu__item:hover {
  background-color: #f1f5f9 !important;
  color: #0f172a !important;
}

.admin-msg-box {
  background-color: #ffffff !important;
  border: 1px solid #e2e8f0 !important;
  border-radius: 12px !important;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.05) !important;
}

.admin-msg-box .el-message-box__title {
  color: #0f172a !important;
}

.admin-msg-box .el-message-box__content {
  color: #475569 !important;
}

.admin-msg-box .el-button {
  border-radius: 6px !important;
  border-color: #cbd5e1 !important;
  background-color: #ffffff !important;
  color: #475569 !important;
}

.admin-msg-box .el-button:hover {
  background-color: #f8fafc !important;
  border-color: #94a3b8 !important;
  color: #0f172a !important;
}

.admin-msg-box .el-button--primary {
  background: linear-gradient(to right, #3b82f6, #4f46e5) !important;
  border: none !important;
  color: white !important;
}

.admin-msg-box .el-button--primary:hover {
  opacity: 0.9 !important;
  color: white !important;
}
</style>
