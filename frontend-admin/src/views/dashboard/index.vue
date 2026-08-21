<template>
  <div class="space-y-6 pb-8">
    <!-- 顶部欢迎区与快捷控制栏 -->
    <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 bg-gradient-to-r from-slate-900 via-blue-950 to-slate-900 text-white p-6 rounded-2xl shadow-xl relative overflow-hidden">
      <div class="absolute -right-10 -bottom-10 w-64 h-64 bg-blue-500/10 rounded-full blur-3xl pointer-events-none"></div>
      <div class="space-y-1 relative z-10">
        <div class="flex items-center gap-3">
          <h1 class="text-2xl font-bold tracking-tight">STP 运营数据指挥中心</h1>
          <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-500/20 text-emerald-300 border border-emerald-500/30">
            <span class="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse mr-1.5"></span>
            微服务集群运行良好
          </span>
        </div>
        <p class="text-sm text-slate-300">实时监控全站交易、会员增长、社区内容风控与系统健康度</p>
      </div>

      <!-- 右侧快捷按钮 -->
      <div class="flex items-center gap-3 relative z-10 shrink-0">
        <el-tooltip content="刷新最新大盘数据" placement="top">
          <button
            @click="fetchDashboardData"
            :disabled="loading"
            class="p-2.5 rounded-xl bg-white/10 hover:bg-white/20 text-white transition-all border border-white/10 active:scale-95 disabled:opacity-50"
          >
            <el-icon :class="{ 'animate-spin': loading }" class="text-lg"><Refresh /></el-icon>
          </button>
        </el-tooltip>
        <button
          @click="openNoticeModal"
          class="flex items-center gap-2 px-4 py-2.5 rounded-xl  bg-black cursor-pointer text-white text-sm font-medium transition-all shadow-lg shadow-blue-600/30 active:scale-95"
        >
          <el-icon class="text-base"><Bell /></el-icon>
          <span>发布全站广播</span>
        </button>
        <button
          @click="openCouponModal"
          class="flex items-center gap-2 px-4 py-2.5 rounded-xl bg-black  cursor-pointer   text-white text-sm font-medium transition-all shadow-lg shadow-blue-600/30 active:scale-95"
        >
          <el-icon class="text-base"><Ticket /></el-icon>
          <span>新建优惠券</span>
        </button>
      </div>
    </div>

    <!-- 核心 KPI 统计卡片 (4列) -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
      <!-- 1. 全站累计用户 -->
      <div class="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm hover:shadow-md transition-all duration-300 relative overflow-hidden group">
        <div class="flex items-center justify-between">
          <span class="text-xs font-semibold text-slate-400 uppercase tracking-wider">全站用户总量</span>
          <div class="w-10 h-10 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center group-hover:scale-110 transition-transform">
            <el-icon class="text-xl"><UserFilled /></el-icon>
          </div>
        </div>
        <div class="mt-4 flex items-baseline gap-2">
          <span class="text-2xl font-extrabold text-slate-800 tracking-tight">{{ formatNumber(overview.totalUsers) }}</span>
          <span class="text-xs text-slate-500">人</span>
        </div>
        <div class="mt-3 flex items-center justify-between text-xs border-t border-slate-100 pt-3">
          <span class="text-slate-500">今日新增 <strong class="text-slate-700">+{{ overview.todayNewUsers }}</strong></span>
          <span class="inline-flex items-center font-medium text-emerald-600 bg-emerald-50 px-1.5 py-0.5 rounded">
            <el-icon class="mr-0.5"><CaretTop /></el-icon> {{ formatRate(overview.userGrowthRate) }}
          </span>
        </div>
      </div>

      <!-- 2. 全站 GMV 成交额 -->
      <div class="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm hover:shadow-md transition-all duration-300 relative overflow-hidden group">
        <div class="flex items-center justify-between">
          <span class="text-xs font-semibold text-slate-400 uppercase tracking-wider">累计 GMV 成交额</span>
          <div class="w-10 h-10 rounded-xl bg-indigo-50 text-indigo-600 flex items-center justify-center group-hover:scale-110 transition-transform">
            <el-icon class="text-xl"><Money /></el-icon>
          </div>
        </div>
        <div class="mt-4 flex items-baseline gap-2">
          <span class="text-2xl font-extrabold text-slate-800 tracking-tight">￥{{ formatMoney(overview.totalGmv) }}</span>
        </div>
        <div class="mt-3 flex items-center justify-between text-xs border-t border-slate-100 pt-3">
          <span class="text-slate-500">今日交易 <strong class="text-slate-700">￥{{ formatMoney(overview.todayGmv) }}</strong></span>
          <span class="inline-flex items-center font-medium text-emerald-600 bg-emerald-50 px-1.5 py-0.5 rounded">
            <el-icon class="mr-0.5"><CaretTop /></el-icon> {{ formatRate(overview.gmvGrowthRate) }}
          </span>
        </div>
      </div>

      <!-- 3. VIP 会员数量 -->
      <div class="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm hover:shadow-md transition-all duration-300 relative overflow-hidden group">
        <div class="flex items-center justify-between">
          <span class="text-xs font-semibold text-slate-400 uppercase tracking-wider">付费 VIP 会员</span>
          <div class="w-10 h-10 rounded-xl bg-amber-50 text-amber-600 flex items-center justify-center group-hover:scale-110 transition-transform">
            <el-icon class="text-xl"><Medal /></el-icon>
          </div>
        </div>
        <div class="mt-4 flex items-baseline gap-2">
          <span class="text-2xl font-extrabold text-slate-800 tracking-tight">{{ formatNumber(overview.activeMembers) }}</span>
          <span class="text-xs text-slate-500">人</span>
        </div>
        <div class="mt-3 flex items-center justify-between text-xs border-t border-slate-100 pt-3">
          <span class="text-slate-500">今日新开 <strong class="text-slate-700">+{{ overview.todayNewMembers }}</strong></span>
          <span class="inline-flex items-center font-medium text-emerald-600 bg-emerald-50 px-1.5 py-0.5 rounded">
            <el-icon class="mr-0.5"><CaretTop /></el-icon> {{ formatRate(overview.memberGrowthRate) }}
          </span>
        </div>
      </div>

      <!-- 4. 待处理违规/举报预警 -->
      <div class="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-sm hover:shadow-md transition-all duration-300 relative overflow-hidden group">
        <div class="flex items-center justify-between">
          <span class="text-xs font-semibold text-slate-400 uppercase tracking-wider">待处理风控举报</span>
          <div class="w-10 h-10 rounded-xl bg-rose-50 text-rose-600 flex items-center justify-center group-hover:scale-110 transition-transform">
            <el-icon class="text-xl"><Warning /></el-icon>
          </div>
        </div>
        <div class="mt-4 flex items-baseline gap-2">
          <span class="text-2xl font-extrabold text-rose-600 tracking-tight">{{ overview.pendingReports }}</span>
          <span class="text-xs text-slate-500">件</span>
        </div>
        <div class="mt-3 flex items-center justify-between text-xs border-t border-slate-100 pt-3">
          <span class="text-slate-500">今日已处理 <strong class="text-slate-700">{{ overview.todayProcessedReports }} 件</strong></span>
          <span class="inline-flex items-center font-medium text-emerald-600 bg-emerald-50 px-1.5 py-0.5 rounded">
            <el-icon class="mr-0.5"><CaretBottom /></el-icon> {{ formatRate(overview.reportGrowthRate) }}
          </span>
        </div>
      </div>
    </div>

    <!-- 图表第 1 排: 业务走势 (70%) + 会员分布 (30%) -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- 趋势图 -->
      <div class="lg:col-span-2 bg-white p-6 rounded-2xl border border-slate-200/80 shadow-sm flex flex-col">
        <div class="flex items-center justify-between mb-4">
          <div>
            <h2 class="text-base font-bold text-slate-800">全站 GMV 交易额与订单趋势</h2>
            <p class="text-xs text-slate-400">成交额 (￥) 与每日订单笔数对比分析</p>
          </div>
          <div class="flex items-center bg-slate-100 p-1 rounded-xl gap-1">
            <button
              v-for="p in ['7d', '30d', 'year']"
              :key="p"
              @click="changeTrendPeriod(p)"
              class="px-3 py-1 text-xs rounded-lg font-medium transition-all"
              :class="trendPeriod === p ? 'bg-white text-blue-600 shadow-sm' : 'text-slate-500 hover:text-slate-800'"
            >
              {{ p === '7d' ? '近7天' : p === '30d' ? '近30天' : '本年度' }}
            </button>
          </div>
        </div>
        <div ref="trendsChartRef" class="w-full h-72 flex-1 min-h-[280px]"></div>
      </div>

      <!-- 会员分布与套餐结构图 (共 3 个 Tab) -->
      <div class="bg-white p-6 rounded-2xl border border-slate-200/80 shadow-sm flex flex-col">
        <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-2 mb-4">
          <div>
            <h2 class="text-base font-bold text-slate-800">会员等级与套餐结构</h2>
            <p class="text-xs text-slate-400">会员套餐销量与 VIP 等级分布占比</p>
          </div>
          <div class="flex items-center bg-slate-100 p-1 rounded-xl gap-1 shrink-0">
            <button
              @click="changeMemberTab('type_1')"
              class="px-2.5 py-1 text-xs rounded-lg font-medium transition-all"
              :class="activeMemberTab === 'type_1' ? 'bg-white text-blue-600 shadow-sm' : 'text-slate-500 hover:text-slate-800'"
            >
              普通会员
            </button>
            <button
              @click="changeMemberTab('type_2')"
              class="px-2.5 py-1 text-xs rounded-lg font-medium transition-all"
              :class="activeMemberTab === 'type_2' ? 'bg-white text-blue-600 shadow-sm' : 'text-slate-500 hover:text-slate-800'"
            >
              超级会员
            </button>
            <button
              @click="changeMemberTab('level')"
              class="px-2.5 py-1 text-xs rounded-lg font-medium transition-all"
              :class="activeMemberTab === 'level' ? 'bg-white text-blue-600 shadow-sm' : 'text-slate-500 hover:text-slate-800'"
            >
              等级水平
            </button>
          </div>
        </div>
        <div ref="memberStatsChartRef" class="w-full h-72 flex-1 min-h-[280px]"></div>
      </div>
    </div>

    <!-- 图表第 2 排: 社区内容活跃度 (50%) + 系统微服务健康度 (50%) -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- 社区互动与风控图 -->
      <div class="bg-white p-6 rounded-2xl border border-slate-200/80 shadow-sm flex flex-col">
        <div class="flex items-center justify-between mb-4">
          <div>
            <h2 class="text-base font-bold text-slate-800">社区内容互动与违规拦截走势</h2>
            <p class="text-xs text-slate-400">每日发帖量、评论量与系统自动拦截对比</p>
          </div>
          <span class="text-xs text-slate-400 bg-slate-50 px-2.5 py-1 rounded-md border border-slate-100">近 7 天数据</span>
        </div>
        <div ref="contentStatsChartRef" class="w-full h-64 flex-1 min-h-[260px]"></div>
      </div>

      <!-- 微服务系统监控面板 -->
      <div class="bg-white p-6 rounded-2xl border border-slate-200/80 shadow-sm flex flex-col justify-between">
        <div>
          <div class="flex items-center justify-between mb-4">
            <div>
              <h2 class="text-base font-bold text-slate-800">微服务集群与实时性能监控</h2>
              <p class="text-xs text-slate-400">核心微服务响应时间、网关 QPS 与资源占用</p>
            </div>
            <span class="inline-flex items-center text-xs text-blue-600 bg-blue-50 px-2.5 py-1 rounded-md font-medium">
              网关 QPS: {{ systemStatus.gatewayQps }}
            </span>
          </div>

          <!-- 4 项基础指标 Progress 条 -->
          <div class="grid grid-cols-2 gap-4 mb-6">
            <div class="bg-slate-50 p-3.5 rounded-xl border border-slate-100">
              <div class="flex justify-between text-xs text-slate-500 mb-1.5">
                <span>平均 API 延迟</span>
                <span class="font-bold text-slate-700">{{ systemStatus.avgResponseTimeMs }} ms</span>
              </div>
              <el-progress :percentage="Math.min(systemStatus.avgResponseTimeMs * 2, 100)" :show-text="false" status="success" />
            </div>

            <div class="bg-slate-50 p-3.5 rounded-xl border border-slate-100">
              <div class="flex justify-between text-xs text-slate-500 mb-1.5">
                <span>CPU 平均使用率</span>
                <span class="font-bold text-slate-700">{{ systemStatus.cpuUsage }}%</span>
              </div>
              <el-progress :percentage="systemStatus.cpuUsage" :show-text="false" color="#3b82f6" />
            </div>

            <div class="bg-slate-50 p-3.5 rounded-xl border border-slate-100">
              <div class="flex justify-between text-xs text-slate-500 mb-1.5">
                <span>JVM 内存占用率</span>
                <span class="font-bold text-slate-700">{{ systemStatus.memoryUsage }}%</span>
              </div>
              <el-progress :percentage="systemStatus.memoryUsage" :show-text="false" color="#8b5cf6" />
            </div>

            <div class="bg-slate-50 p-3.5 rounded-xl border border-slate-100">
              <div class="flex justify-between text-xs text-slate-500 mb-1.5">
                <span>Redis 缓存命中率</span>
                <span class="font-bold text-slate-700">{{ systemStatus.redisHitRate }}%</span>
              </div>
              <el-progress :percentage="systemStatus.redisHitRate" :show-text="false" color="#10b981" />
            </div>
          </div>

          <!-- 微服务节点列表 (小 Grid) -->
          <h3 class="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-2.5">微服务节点存活度 (UP)</h3>
          <div class="grid grid-cols-2 sm:grid-cols-3 gap-2">
            <div
              v-for="svc in systemStatus.services"
              :key="svc.name"
              class="flex items-center justify-between px-3 py-2 rounded-lg bg-slate-50 border border-slate-100 text-xs"
            >
              <div class="flex items-center gap-2 truncate">
                <span class="w-2 h-2 rounded-full bg-emerald-500 shrink-0"></span>
                <span class="font-medium text-slate-700 truncate">{{ svc.name }}</span>
              </div>
              <span class="text-slate-400 font-mono text-[11px] shrink-0 ml-1">{{ svc.instances }}实例</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部区域: 实时风控与系统动态列表 -->
    <div class="bg-white p-6 rounded-2xl border border-slate-200/80 shadow-sm">
      <div class="flex items-center justify-between mb-4">
        <div>
          <h2 class="text-base font-bold text-slate-800">实时风险预警与操作动态</h2>
          <p class="text-xs text-slate-400">全站风控告警、大额充值与审核动态提示</p>
        </div>
        <span class="text-xs text-slate-400">实时流转日志</span>
      </div>

      <div class="divide-y divide-slate-100">
        <div
          v-for="act in activities"
          :key="act.id"
          class="py-3 flex items-center justify-between gap-4 hover:bg-slate-50/80 px-2 rounded-xl transition-colors"
        >
          <div class="flex items-center gap-3.5 overflow-hidden">
            <div
              class="w-9 h-9 rounded-xl flex items-center justify-center shrink-0"
              :class="{
                'bg-rose-50 text-rose-600': act.type === 'WARN',
                'bg-emerald-50 text-emerald-600': act.type === 'ORDER',
                'bg-amber-50 text-amber-600': act.type === 'AUDIT',
                'bg-blue-50 text-blue-600': act.type === 'INFO'
              }"
            >
              <el-icon class="text-lg">
                <Warning v-if="act.type === 'WARN'" />
                <ShoppingCart v-else-if="act.type === 'ORDER'" />
                <Document v-else-if="act.type === 'AUDIT'" />
                <InfoFilled v-else />
              </el-icon>
            </div>

            <div class="space-y-0.5 overflow-hidden">
              <div class="flex items-center gap-2">
                <span class="text-sm font-semibold text-slate-800">{{ act.title }}</span>
                <span class="text-xs text-slate-400 font-mono">{{ act.createTime }}</span>
              </div>
              <p class="text-xs text-slate-500 truncate">{{ act.content }}</p>
            </div>
          </div>

          <button
            v-if="act.targetUrl"
            @click="router.push(act.targetUrl)"
            class="px-3 py-1.5 rounded-lg text-xs text-blue-600 hover:bg-blue-50 font-medium transition-colors shrink-0"
          >
            前往处理
          </button>
        </div>
      </div>
    </div>

    <!-- 广播全站通知 弹窗 -->
    <el-dialog v-model="noticeModalVisible" title="发布全站通知广播" width="500px" custom-class="admin-dialog">
      <el-form :model="noticeForm" label-position="top">
        <el-form-item label="通知标题" required>
          <el-input v-model="noticeForm.title" placeholder="请输入广播标题" />
        </el-form-item>
        <el-form-item label="通知类型">
          <el-select v-model="noticeForm.noticeType" class="w-full">
            <el-option label="系统通知" :value="1" />
            <el-option label="活动通知" :value="2" />
            <el-option label="维护公告" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="通知内容" required>
          <el-input v-model="noticeForm.content" type="textarea" :rows="4" placeholder="请输入要广播通知的内容..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-2">
          <el-button @click="noticeModalVisible = false">取消</el-button>
          <el-button type="primary" :loading="noticeSubmitting" @click="handleSendNotice">立即发送广播</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 新建优惠券 弹窗 -->
    <el-dialog v-model="couponModalVisible" title="快捷新建优惠券" width="500px" custom-class="admin-dialog">
      <el-form :model="couponForm" label-width="100px">
        <el-form-item label="优惠券名称" required>
          <el-input v-model="couponForm.name" placeholder="例: 新用户立减券" />
        </el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="couponForm.type">
            <el-radio :value="1">满减券(金额)</el-radio>
            <el-radio :value="0">折扣券(折扣)</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="面值/金额(元)" required>
          <el-input-number v-model="couponForm.amount" :min="1" :precision="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-2">
          <el-button @click="couponModalVisible = false">取消</el-button>
          <el-button type="primary" :loading="couponSubmitting" @click="handleSaveCoupon">确定创建</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, onUnmounted, reactive, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Refresh,
  Bell,
  Ticket,
  UserFilled,
  Money,
  Medal,
  Warning,
  CaretTop,
  CaretBottom,
  ShoppingCart,
  Document,
  InfoFilled
} from '@element-plus/icons-vue'
import {
  DashboardAPI,
  type DashboardOverview,
  type DashboardTrends,
  type DashboardMemberStats,
  type DashboardContentStats,
  type DashboardSystemStatus,
  type DashboardActivity
} from '@/services/dashboard'

const router = useRouter()
const loading = ref(false)

const trendPeriod = ref('30d')
const activeMemberTab = ref('type_1')
const rawMemberStatsData = ref<DashboardMemberStats | null>(null)

// 页面核心响应式 State
const overview = ref<DashboardOverview>({
  totalUsers: 0,
  todayNewUsers: 0,
  userGrowthRate: 0,
  totalGmv: 0,
  todayGmv: 0,
  gmvGrowthRate: 0,
  activeMembers: 0,
  todayNewMembers: 0,
  memberGrowthRate: 0,
  pendingReports: 0,
  todayProcessedReports: 0,
  reportGrowthRate: 0
})

const systemStatus = ref<DashboardSystemStatus>({
  gatewayQps: 0,
  avgResponseTimeMs: 0,
  cpuUsage: 0,
  memoryUsage: 0,
  redisHitRate: 0,
  mqBacklogCount: 0,
  services: []
})

const activities = ref<DashboardActivity[]>([])

// ECharts DOM 节点引用与实例
const trendsChartRef = ref<HTMLDivElement | null>(null)
const memberStatsChartRef = ref<HTMLDivElement | null>(null)
const contentStatsChartRef = ref<HTMLDivElement | null>(null)

let trendsChartInstance: any = null
let memberStatsChartInstance: any = null
let contentStatsChartInstance: any = null

// 数字、金额与百分比格式化函数
const formatNumber = (val: number | string | undefined | null) => {
  if (val === null || val === undefined) return '0'
  const num = typeof val === 'number' ? val : parseFloat(val)
  return isNaN(num) ? '0' : num.toLocaleString('en-US')
}
const formatMoney = (val: number | string | undefined | null) => {
  if (val === null || val === undefined) return '0.00'
  const num = typeof val === 'number' ? val : parseFloat(val)
  return isNaN(num) ? '0.00' : num.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
const formatRate = (val: number | string | undefined | null) => {
  if (val === null || val === undefined) return '0.0%'
  if (typeof val === 'string') {
    return val.endsWith('%') ? val : `${val}%`
  }
  return `${val.toFixed(1)}%`
}

// 获取全盘数据
const fetchDashboardData = async () => {
  loading.value = true
  try {
    const [ovRes, trendsRes, memberRes, contentRes, sysRes, actRes] = await Promise.all([
      DashboardAPI.getOverview(),
      DashboardAPI.getTrends(trendPeriod.value),
      DashboardAPI.getMemberStats(),
      DashboardAPI.getContentStats(),
      DashboardAPI.getSystemStatus(),
      DashboardAPI.getRecentActivities()
    ])

    if (ovRes.data) overview.value = ovRes.data
    if (sysRes.data) systemStatus.value = sysRes.data
    if (actRes.data) activities.value = actRes.data

    await nextTick()

    setTimeout(async () => {
      if (trendsRes.data) await renderTrendsChart(trendsRes.data)
      if (memberRes.data) await renderMemberStatsChart(memberRes.data)
      if (contentRes.data) await renderContentStatsChart(contentRes.data)
    }, 50)
  } catch (err) {
    ElMessage.error('获取大盘数据失败，请重试')
  } finally {
    loading.value = false
  }
}

const changeTrendPeriod = async (period: string) => {
  trendPeriod.value = period
  const res = await DashboardAPI.getTrends(period)
  if (res.data) renderTrendsChart(res.data)
}

let echartsLib: any = null
const getEcharts = async () => {
  if (echartsLib) return echartsLib
  if ((window as any).echarts) {
    echartsLib = (window as any).echarts
    return echartsLib
  }
  try {
    const pkgName = 'echarts'
    const mod = await import(/* @vite-ignore */ pkgName)
    echartsLib = mod ? (mod.default || mod) : null
    if (echartsLib && echartsLib.init) return echartsLib
  } catch {}

  if (!(window as any)._echartsLoadPromise) {
    (window as any)._echartsLoadPromise = new Promise((resolve) => {
      if ((window as any).echarts) {
        resolve((window as any).echarts)
        return
      }
      const script = document.createElement('script')
      script.src = 'https://cdn.jsdelivr.net/npm/echarts@5.5.0/dist/echarts.min.js'
      script.onload = () => resolve((window as any).echarts)
      script.onerror = () => resolve(null)
      document.head.appendChild(script)
    })
  }
  echartsLib = await (window as any)._echartsLoadPromise
  return echartsLib
}

// 1. 渲染 GMV 走势双轴图 (Bar + Line)
const renderTrendsChart = async (data: DashboardTrends) => {
  if (!trendsChartRef.value) return
  const echarts = await getEcharts()
  if (!echarts) return

  if (!trendsChartInstance) {
    trendsChartInstance = echarts.init(trendsChartRef.value)
  }

  const option: any = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(15, 23, 42, 0.85)',
      borderColor: '#334155',
      textStyle: { color: '#f8fafc' },
      axisPointer: { type: 'shadow' }
    },
    legend: {
      data: ['GMV 成交额(￥)', '订单笔数'],
      top: 0,
      right: 10,
      textStyle: { color: '#64748b' }
    },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '15%', containLabel: true },
    xAxis: {
      type: 'category',
      data: data.dates,
      axisLine: { lineStyle: { color: '#cbd5e1' } },
      axisLabel: { color: '#64748b' }
    },
    yAxis: [
      {
        type: 'value',
        name: 'GMV (￥)',
        splitLine: { lineStyle: { color: '#f1f5f9' } },
        axisLabel: { color: '#64748b' }
      },
      {
        type: 'value',
        name: '订单数',
        splitLine: { show: false },
        axisLabel: { color: '#64748b' }
      }
    ],
    series: [
      {
        name: 'GMV 成交额(￥)',
        type: 'bar',
        barWidth: '40%',
        data: data.gmvList,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#6366f1' },
            { offset: 1, color: '#a5b4fc' }
          ]),
          borderRadius: [6, 6, 0, 0]
        }
      },
      {
        name: '订单笔数',
        type: 'line',
        yAxisIndex: 1,
        smooth: true,
        data: data.orderCountList,
        symbolSize: 6,
        itemStyle: { color: '#10b981' },
        lineStyle: { width: 3, color: '#10b981' }
      }
    ]
  }

  trendsChartInstance.setOption(option)
}

const changeMemberTab = async (tabKey: string) => {
  activeMemberTab.value = tabKey
  try {
    const res = await DashboardAPI.getMemberStats(tabKey)
    if (res.data) {
      renderMemberStatsChart(res.data)
    }
  } catch {
    if (rawMemberStatsData.value) {
      renderMemberStatsChart(rawMemberStatsData.value)
    }
  }
}

// 2. 渲染会员等级与套餐占比 饼图 (支持 3 个 Tab: 普通会员套餐、超级会员套餐、VIP等级水平)
const renderMemberStatsChart = async (data: DashboardMemberStats) => {
  rawMemberStatsData.value = data
  if (!memberStatsChartRef.value) return
  const echarts = await getEcharts()
  if (!echarts) return

  if (!memberStatsChartInstance) {
    memberStatsChartInstance = echarts.init(memberStatsChartRef.value)
  }

  let chartTitle = '会员分布'
  let chartData: { name: string; value: number }[] = []

  if (activeMemberTab.value === 'level' || !data.typePackageGroups || data.typePackageGroups.length === 0) {
    chartTitle = 'VIP 等级人数占比'
    chartData = (data.levelDistribution || []).map(item => ({
      name: item.levelName,
      value: item.count
    }))
  } else if (activeMemberTab.value === 'type_1') {
    const group1 = data.typePackageGroups.find(g => g.typeId === 1)
    chartTitle = group1 ? `${group1.typeName}套餐` : '普通会员套餐'
    if (group1 && group1.packages && group1.packages.length > 0) {
      chartData = group1.packages.map(p => ({
        name: p.packageName,
        value: p.count
      }))
    }
  } else if (activeMemberTab.value === 'type_2') {
    const group2 = data.typePackageGroups.find(g => g.typeId === 2)
    chartTitle = group2 ? `${group2.typeName}套餐` : '超级会员套餐'
    if (group2 && group2.packages && group2.packages.length > 0) {
      chartData = group2.packages.map(p => ({
        name: p.packageName,
        value: p.count
      }))
    }
  }

  const option: any = {
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(15, 23, 42, 0.85)',
      textStyle: { color: '#f8fafc' },
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      bottom: '0%',
      left: 'center',
      textStyle: { color: '#64748b', fontSize: 12 }
    },
    color: ['#3b82f6', '#8b5cf6', '#f59e0b', '#10b981', '#ec4899', '#6366f1'],
    series: [
      {
        name: chartTitle,
        type: 'pie',
        radius: ['45%', '72%'],
        center: ['50%', '42%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 8,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: { show: false },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold',
            color: '#1e293b'
          }
        },
        data: chartData
      }
    ]
  }

  memberStatsChartInstance.setOption(option, true)
}

// 3. 渲染社区互动 堆叠面积折线图
const renderContentStatsChart = async (data: DashboardContentStats) => {
  if (!contentStatsChartRef.value) return
  const echarts = await getEcharts()
  if (!echarts) return

  if (!contentStatsChartInstance) {
    contentStatsChartInstance = echarts.init(contentStatsChartRef.value)
  }

  const option: any = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(15, 23, 42, 0.85)',
      textStyle: { color: '#f8fafc' }
    },
    legend: {
      data: ['发布帖子', '评论互动', '拦截违规件'],
      top: 0,
      right: 0,
      textStyle: { color: '#64748b' }
    },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '15%', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: data.dates,
      axisLabel: { color: '#64748b' }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#f1f5f9' } },
      axisLabel: { color: '#64748b' }
    },
    series: [
      {
        name: '发布帖子',
        type: 'line',
        smooth: true,
        data: data.postCountList,
        areaStyle: { opacity: 0.2 },
        itemStyle: { color: '#3b82f6' }
      },
      {
        name: '评论互动',
        type: 'line',
        smooth: true,
        data: data.commentCountList,
        areaStyle: { opacity: 0.2 },
        itemStyle: { color: '#8b5cf6' }
      },
      {
        name: '拦截违规件',
        type: 'line',
        smooth: true,
        data: data.blockedCountList,
        itemStyle: { color: '#f43f5e' }
      }
    ]
  }

  contentStatsChartInstance.setOption(option)
}

// 图表响应式 Resize
const handleResize = () => {
  trendsChartInstance?.resize()
  memberStatsChartInstance?.resize()
  contentStatsChartInstance?.resize()
}

// 发布全站广播 弹窗 State
const noticeModalVisible = ref(false)
const noticeSubmitting = ref(false)
const noticeForm = reactive({
  title: '',
  noticeType: 1,
  content: ''
})

const openNoticeModal = () => {
  noticeForm.title = ''
  noticeForm.content = ''
  noticeModalVisible.value = true
}

const handleSendNotice = async () => {
  if (!noticeForm.title || !noticeForm.content) {
    ElMessage.warning('请补全通知标题与广播内容')
    return
  }
  noticeSubmitting.value = true
  try {
    const { NotificationAPI } = await import('@/services/notification')
    const res = await NotificationAPI.createNotification({
      title: noticeForm.title,
      content: noticeForm.content,
      noticeType: noticeForm.noticeType,
      targetType: 1
    })
    if (res.code === 1) {
      noticeModalVisible.value = false
    } else {
      ElMessage.error(res.errMsg || '发布失败')
    }
  } catch {
    noticeModalVisible.value = false
  } finally {
    noticeSubmitting.value = false
  }
}

// 新建优惠券 弹窗 State
const couponModalVisible = ref(false)
const couponSubmitting = ref(false)
const couponForm = reactive({
  name: '',
  type: 1,
  amount: 10
})

const openCouponModal = () => {
  couponForm.name = ''
  couponForm.amount = 10
  couponModalVisible.value = true
}

const handleSaveCoupon = async () => {
  if (!couponForm.name) {
    ElMessage.warning('请输入优惠券名称')
    return
  }
  couponSubmitting.value = true
  try {
    const { CouponAPI } = await import('@/services/coupon')
    const res = await CouponAPI.saveCoupon({
      name: couponForm.name,
      type: couponForm.type,
      amount: couponForm.amount
    })
    if (res.code === 1) {
      couponModalVisible.value = false
    } else {
      ElMessage.error(res.errMsg || '创建失败')
    }
  } catch {
    couponModalVisible.value = false
  } finally {
    couponSubmitting.value = false
  }
}

onMounted(() => {
  fetchDashboardData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendsChartInstance?.dispose()
  memberStatsChartInstance?.dispose()
  contentStatsChartInstance?.dispose()
})
</script>

<style scoped>
.admin-dialog :deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
}
</style>
