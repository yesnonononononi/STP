<template>
  <div class="space-y-6 animate-fade-in text-slate-800">
    <!-- 头部卡片 / 工具栏 -->
    <div class="p-6 rounded-xl border border-slate-200 bg-white shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-4">
      <div>
        <h3 class="text-xl font-semibold text-slate-800">管理员列表</h3>
        <p class="text-sm text-slate-500 mt-1">管理系统内部的管理员人员及其等级、状态。</p>
      </div>
      
      <div class="flex items-center gap-3">
        <el-input
          v-model="searchQuery"
          placeholder="搜索管理员账号..."
          :prefix-icon="Search"
          clearable
          class="admin-search-input w-64"
        />
        <el-button 
          type="primary" 
          :icon="Refresh"
          class="admin-btn-secondary"
          @click="fetchAdminList"
        >
          刷新
        </el-button>
      </div>
    </div>

    <!-- 数据表格卡片 -->
    <div class="rounded-xl border border-slate-200 bg-white shadow-xs overflow-hidden p-6">
      <el-table 
        v-loading="loading" 
        :data="filteredList" 
        class="admin-table-custom"
        style="width: 100%"
      >
        <el-table-column prop="id" label="记录 ID" width="100" />
        <el-table-column prop="userId" label="用户 ID" width="120" />
        
        <el-table-column prop="username" label="管理员账号" min-width="150">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <span class="font-medium text-slate-800">{{ row.username }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="order" label="管理员等级" width="150">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <el-tag 
                :type="getOrderTagType(row.order)" 
                effect="dark" 
                class="admin-order-tag"
              >
                Level {{ row.order }}
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="当前状态" width="120">
          <template #default="{ row }">
            <span class="flex items-center gap-1.5">
              <span 
                class="w-2 h-2 rounded-full" 
                :class="row.status === 1 ? 'bg-emerald-500 shadow-xs shadow-emerald-500/50' : 'bg-rose-500 shadow-xs shadow-rose-500/50'"
              ></span>
              <span :class="row.status === 1 ? 'text-emerald-600' : 'text-rose-600'" class="text-sm font-medium">
                {{ row.status === 1 ? '正常' : '已禁用' }}
              </span>
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="创建时间" min-width="180">
          <template #default="{ row }">
            <span class="text-slate-500 text-sm">{{ formatTime(row.createTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center gap-2.5">
              <!-- 启用 / 禁用 -->
              <el-button 
                v-if="row.status === 1"
                size="small" 
                type="danger" 
                plain
                class="admin-action-btn-danger"
                @click="handleToggleBan(row, true)"
              >
                禁用
              </el-button>
              <el-button 
                v-else
                size="small" 
                type="success" 
                plain
                class="admin-action-btn-success"
                @click="handleToggleBan(row, false)"
              >
                启用
              </el-button>

              <!-- 升降级 -->
              <el-button 
                size="small" 
                type="primary" 
                :icon="CaretTop"
                class="admin-action-btn"
                @click="handleOrderChange(row, true)"
              >
                升级
              </el-button>
              <el-button 
                size="small" 
                type="warning" 
                :icon="CaretBottom"
                class="admin-action-btn-warning"
                @click="handleOrderChange(row, false)"
              >
                降级
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页栏 -->
      <div class="flex justify-end mt-6">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          class="admin-pagination"
          @size-change="fetchAdminList"
          @current-change="fetchAdminList"
        />
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {ref, onMounted, computed} from 'vue'
import {ElMessageBox} from 'element-plus'
import {Search, Refresh, CaretTop, CaretBottom} from '@element-plus/icons-vue'
import request from '@/services/request'

interface AdminVO {
  id: number
  userId: number
  username: string
  status: number
  order: number
  createTime: string
  updateTime: string
}

const list = ref<AdminVO[]>([])
const loading = ref(false)
const searchQuery = ref('')

const page = ref(1)
const pageSize = ref(10)
const total = ref(0) // 后端列表若是假分页/全量，可以在这里做本地计算

// 本地模糊搜索
const filteredList = computed(() => {
  if (!searchQuery.value) return list.value
  const query = searchQuery.value.toLowerCase()
  return list.value.filter(item => 
    item.username.toLowerCase().includes(query) || 
    item.userId.toString().includes(query)
  )
})

const getOrderTagType = (order: number) => {
  if (order >= 3) return 'danger'
  if (order >= 2) return 'warning'
  return 'primary'
}

const formatTime = (timeStr: string) => {
  if (!timeStr) return '-'
  try {
    const date = new Date(timeStr)
    return date.toLocaleString('zh-CN', { timeZone: 'Asia/Shanghai' })
  } catch {
    return timeStr
  }
}

// 获取管理员列表
const fetchAdminList = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/u/admin/list', {
      params: {
        page: page.value,
        pageSize: pageSize.value
      }
    })
    if (res.code === 1) {
      list.value = res.data || []
      // 后端当前未返回 total，这里用“是否满页”推导下一页是否可点击。
      total.value = list.value.length < pageSize.value
        ? (page.value - 1) * pageSize.value + list.value.length
        : page.value * pageSize.value + 1
    }
  } catch {
    // 错误在 Axios 响应拦截器统一集中处理
  } finally {
    loading.value = false
  }
}

// 禁用 / 启用操作
const handleToggleBan = (row: AdminVO, attemptBan: boolean) => {
  const actionText = attemptBan ? '禁用' : '启用'
  ElMessageBox.confirm(
    `确定要${actionText}管理员账号 [${row.username}] 吗？`,
    '安全操作确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      customClass: 'admin-msg-box'
    }
  ).then(async () => {
    try {
      const path = attemptBan ? '/u/admin/ban' : '/u/admin/unban'
      const res: any = await request.post(path, null, {
        params: { aId: row.id }
      })
      if (res.code === 1) {
        fetchAdminList()
      }
    } catch {
      // 错误在 Axios 响应拦截器统一集中处理
    }
  }).catch(() => {})
}

// 等级升降操作
const handleOrderChange = async (row: AdminVO, attemptAscend: boolean) => {
  const actionText = attemptAscend ? '提升' : '降低'
  try {
    const path = attemptAscend ? '/u/admin/ascend' : '/u/admin/descend'
    const res: any = await request.post(path, null, {
      params: { aId: row.id }
    })
    if (res.code === 1) {
      fetchAdminList()
    }
  } catch {
    // 错误在 Axios 响应拦截器统一集中处理
  }
}

onMounted(() => {
  fetchAdminList()
})
</script>

<style>
/* 搜索框亮色重构 */
.admin-search-input .el-input__wrapper {
  background-color: rgba(255, 255, 255, 0.8) !important;
  box-shadow: 0 0 0 1px rgba(203, 213, 225, 0.8) inset !important;
  border-radius: 8px !important;
  transition: all 0.3s ease !important;
}

.admin-search-input .el-input__wrapper:hover {
  box-shadow: 0 0 0 1px rgba(6, 182, 212, 0.5) inset !important;
}

.admin-search-input .el-input__inner {
  color: #1e293b !important;
}

/* 按钮的亮色定制 */
.admin-btn-secondary {
  background-color: #ffffff !important;
  border-color: #cbd5e1 !important;
  color: #475569 !important;
  border-radius: 8px !important;
  transition: all 0.3s ease !important;
}

.admin-btn-secondary:hover {
  background-color: #f8fafc !important;
  color: #0f172a !important;
  border-color: #94a3b8 !important;
}

/* 表格亮色定制 */
.admin-table-custom {
  background-color: #ffffff !important;
  --el-table-border-color: #e2e8f0 !important;
  --el-table-header-bg-color: #f8fafc !important;
  --el-table-row-hover-bg-color: rgba(241, 245, 249, 0.6) !important;
}

.admin-table-custom th.el-table__cell {
  color: #475569 !important;
  font-weight: 600 !important;
  border-bottom: 1px solid #e2e8f0 !important;
}

.admin-table-custom td.el-table__cell {
  color: #334155 !important;
  border-bottom: 1px solid #f1f5f9 !important;
}

/* Tag 样式 */
.admin-order-tag {
  border-radius: 6px !important;
  padding: 2px 8px !important;
}

/* 操作按钮 */
.admin-action-btn {
  background-color: rgba(6, 182, 212, 0.1) !important;
  border: 1px solid rgba(6, 182, 212, 0.25) !important;
  color: #0891b2 !important;
  border-radius: 6px !important;
}

.admin-action-btn:hover {
  background-color: #06b6d4 !important;
  color: white !important;
  border-color: #06b6d4 !important;
}

.admin-action-btn-warning {
  background-color: rgba(245, 158, 11, 0.1) !important;
  border: 1px solid rgba(245, 158, 11, 0.25) !important;
  color: #d97706 !important;
  border-radius: 6px !important;
}

.admin-action-btn-warning:hover {
  background-color: #f59e0b !important;
  color: white !important;
  border-color: #f59e0b !important;
}

.admin-action-btn-danger {
  background-color: rgba(239, 68, 68, 0.08) !important;
  border-color: rgba(239, 68, 68, 0.25) !important;
  color: #dc2626 !important;
  border-radius: 6px !important;
}

.admin-action-btn-danger:hover {
  background-color: #ef4444 !important;
  color: white !important;
  border-color: #ef4444 !important;
}

.admin-action-btn-success {
  background-color: rgba(16, 185, 129, 0.08) !important;
  border-color: rgba(16, 185, 129, 0.25) !important;
  color: #059669 !important;
  border-radius: 6px !important;
}

.admin-action-btn-success:hover {
  background-color: #10b981 !important;
  color: white !important;
  border-color: #10b981 !important;
}

/* 分页器 */
.admin-pagination .el-pagination__total,
.admin-pagination .el-pagination__goto,
.admin-pagination .el-input__inner {
  color: #475569 !important;
}

.admin-pagination button {
  background-color: #ffffff !important;
  border: 1px solid #e2e8f0 !important;
  color: #475569 !important;
  border-radius: 6px !important;
  margin: 0 3px !important;
}

.admin-pagination button:disabled {
  background-color: #f8fafc !important;
  color: #cbd5e1 !important;
}

.admin-pagination .el-pager li {
  background-color: #ffffff !important;
  border: 1px solid #e2e8f0 !important;
  color: #475569 !important;
  border-radius: 6px !important;
  margin: 0 3px !important;
}

.admin-pagination .el-pager li.is-active {
  background: linear-gradient(to right, #3b82f6, #4f46e5) !important;
  border-color: transparent !important;
  color: white !important;
}
</style>
