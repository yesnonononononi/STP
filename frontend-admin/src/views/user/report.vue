<template>
  <div class="p-6 space-y-6 bg-slate-50 min-h-screen">
    <!-- 页头标题 -->
    <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold text-slate-800 tracking-tight flex items-center gap-2">
          <el-icon class="text-rose-500"><Warning /></el-icon>
          用户举报审核管理
        </h1>
        <p class="text-xs text-slate-500 mt-1">审核处置违规举报、一键封禁恶意违规用户，维护社区健康秩序</p>
      </div>

      <div class="flex items-center gap-2">
        <el-button type="primary" plain @click="fetchData" class="rounded-xl">
          <el-icon class="mr-1"><Refresh /></el-icon> 刷新列表
        </el-button>
      </div>
    </div>

    <!-- KPI 统计概览 -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
      <div class="bg-white rounded-2xl p-4 shadow-sm border border-slate-100 flex items-center justify-between">
        <div>
          <p class="text-xs font-semibold text-slate-400">待处理举报</p>
          <h3 class="text-2xl font-bold text-amber-500 mt-1">{{ pendingCount }}</h3>
        </div>
        <div class="w-10 h-10 rounded-xl bg-amber-50 flex items-center justify-center text-amber-500">
          <el-icon class="text-xl"><Clock /></el-icon>
        </div>
      </div>

      <div class="bg-white rounded-2xl p-4 shadow-sm border border-slate-100 flex items-center justify-between">
        <div>
          <p class="text-xs font-semibold text-slate-400">已处置封禁</p>
          <h3 class="text-2xl font-bold text-rose-500 mt-1">{{ processedCount }}</h3>
        </div>
        <div class="w-10 h-10 rounded-xl bg-rose-50 flex items-center justify-center text-rose-500">
          <el-icon class="text-xl"><Lock /></el-icon>
        </div>
      </div>

      <div class="bg-white rounded-2xl p-4 shadow-sm border border-slate-100 flex items-center justify-between">
        <div>
          <p class="text-xs font-semibold text-slate-400">已忽略记录</p>
          <h3 class="text-2xl font-bold text-slate-500 mt-1">{{ ignoredCount }}</h3>
        </div>
        <div class="w-10 h-10 rounded-xl bg-slate-100 flex items-center justify-center text-slate-500">
          <el-icon class="text-xl"><Remove /></el-icon>
        </div>
      </div>

      <div class="bg-white rounded-2xl p-4 shadow-sm border border-slate-100 flex items-center justify-between">
        <div>
          <p class="text-xs font-semibold text-slate-400">累积举报总数</p>
          <h3 class="text-2xl font-bold text-indigo-600 mt-1">{{ total }}</h3>
        </div>
        <div class="w-10 h-10 rounded-xl bg-indigo-50 flex items-center justify-center text-indigo-500">
          <el-icon class="text-xl"><Document /></el-icon>
        </div>
      </div>
    </div>

    <!-- 列表数据卡片 -->
    <div class="bg-white rounded-2xl p-6 shadow-sm border border-slate-100 space-y-4">
      <!-- 状态筛选过滤器 -->
      <div class="flex items-center justify-between">
        <el-radio-group v-model="filterStatus" @change="handleStatusChange" size="large">
          <el-radio-button :value="undefined">全部举报</el-radio-button>
          <el-radio-button :value="0">待处理</el-radio-button>
          <el-radio-button :value="2">已处置 (已封禁)</el-radio-button>
          <el-radio-button :value="1">已忽略</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 数据表格 -->
      <el-table :data="reportList" v-loading="loading" stripe style="width: 100%" class="rounded-xl overflow-hidden">
        <el-table-column prop="id" label="举报ID" width="100" />
        
        <el-table-column label="举报人" min-width="150">
          <template #default="{ row }">
            <div class="flex flex-col">
              <span class="font-semibold text-slate-800">{{ row.reporterNick }}</span>
              <span class="text-xs text-slate-400">ID: {{ row.reporterId }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="被举报人" min-width="150">
          <template #default="{ row }">
            <div class="flex flex-col">
              <span class="font-semibold text-rose-600">{{ row.reportedNick }}</span>
              <span class="text-xs text-slate-400">ID: {{ row.reportedId }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="reason" label="举报原因说明" min-width="220">
          <template #default="{ row }">
            <div class="bg-slate-50 p-2 rounded-lg text-xs text-slate-700 border border-slate-100">
              {{ row.reason }}
            </div>
          </template>
        </el-table-column>

        <el-table-column label="审核状态" width="130">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="warning" effect="light" class="rounded-lg">待处理</el-tag>
            <el-tag v-else-if="row.status === 2" type="danger" effect="light" class="rounded-lg">已封禁处置</el-tag>
            <el-tag v-else type="info" effect="light" class="rounded-lg">已忽略</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="举报时间" width="170" />

        <el-table-column label="管理操作" width="220" fixed="right">
          <template #default="{ row }">
            <div v-if="row.status === 0" class="flex items-center gap-2">
              <el-button type="danger" size="small" @click="handleProcessAndBan(row)" class="rounded-lg">
                封禁被举报人
              </el-button>
              <el-button type="info" plain size="small" @click="handleIgnore(row)" class="rounded-lg">
                忽略
              </el-button>
            </div>
            <div v-else class="text-xs text-slate-400">
              已由系统/管理员完成审核
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页区域 -->
      <div class="flex justify-end pt-4">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="fetchData"
          @current-change="fetchData"
          class="rounded-xl"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Warning, Refresh, Clock, Lock, Remove, Document } from '@element-plus/icons-vue'
import { AdminUserReportAPI, type UserReportItem } from '@/services/user'

const loading = ref(false)
const reportList = ref<UserReportItem[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const filterStatus = ref<number | undefined>(undefined)

const pendingCount = ref(0)
const processedCount = ref(0)
const ignoredCount = ref(0)

const fetchData = async () => {
  loading.value = true
  try {
    const res = await AdminUserReportAPI.queryReportPage({
      status: filterStatus.value,
      page: page.value,
      pageSize: pageSize.value
    })
    if (res.code === 1 && res.data) {
      reportList.value = res.data.data || []
      total.value = res.data.total || 0
      calculateStats()
    }
  } catch (err: any) {
    ElMessage.error(err?.message || '获取举报列表失败')
  } finally {
    loading.value = false
  }
}

const calculateStats = () => {
  pendingCount.value = reportList.value.filter(item => item.status === 0).length
  processedCount.value = reportList.value.filter(item => item.status === 2).length
  ignoredCount.value = reportList.value.filter(item => item.status === 1).length
}

const handleStatusChange = () => {
  page.value = 1
  fetchData()
}

const handleIgnore = async (row: UserReportItem) => {
  try {
    await ElMessageBox.confirm(`确定要忽略此条举报记录（ID: ${row.id}）吗？`, '提示', {
      confirmButtonText: '确认忽略',
      cancelButtonText: '取消',
      type: 'info'
    })
    const res = await AdminUserReportAPI.ignoreReport(row.id)
    if (res.code === 1) {
      ElMessage.success('已成功忽略该举报')
      fetchData()
    } else {
      ElMessage.error(res.errMsg || '忽略失败')
    }
  } catch {}
}

const handleProcessAndBan = async (row: UserReportItem) => {
  try {
    await ElMessageBox.confirm(
      `警告：确定要认定此举报生效并【封禁被举报用户 ${row.reportedNick} (ID: ${row.reportedId})】吗？`,
      '高危操作确认',
      {
        confirmButtonText: '确认封禁并结案',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    const res = await AdminUserReportAPI.processAndBanReport(row.id)
    if (res.code === 1) {
      ElMessage.success('处理成功，已封禁违规用户并结案')
      fetchData()
    } else {
      ElMessage.error(res.errMsg || '处理失败')
    }
  } catch {}
}

onMounted(() => {
  fetchData()
})
</script>
