<template>
  <div class="space-y-6 w-full h-full">
    <div class="rounded-xl border border-slate-200 bg-white shadow-xs overflow-hidden p-6">
      <div class="flex justify-between items-center gap-3 mb-6">
        <div>
          <h3 class="text-xl font-semibold text-slate-800">用户管理</h3>
        </div>
        <div class="flex items-center gap-3">
          <el-button @click="resetFilters">重置</el-button>
          <el-button type="primary" @click="openFilter = true">筛选</el-button>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="list"
        class="admin-table-custom"
        style="width: 100%"
        empty-text="暂无符合条件的用户"
        size="large"
      >
        <el-table-column prop="id" label="用户 ID" min-width="130" />

        <el-table-column label="昵称" min-width="220">
          <template #default="{ row }">
            <div class="flex items-center gap-3">
              <el-avatar :src="row.avatar || undefined" :size="36">
                {{ (row.nick || '?').slice(0, 1) }}
              </el-avatar>
              <div class="min-w-0">
                <div class="font-medium text-slate-800 truncate">{{ row.nick || '-' }}</div>
                <div class="text-xs text-slate-500 truncate">{{ row.introduction || '暂无简介' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="性别 / 年龄" width="120">
          <template #default="{ row }">
            <span class="text-slate-700">
              {{ formatGenderAndAge(row.gender, row.age) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="phone" label="手机号" min-width="140">
          <template #default="{ row }">
            {{ row.phone || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="180">
          <template #default="{ row }">
            {{ row.email || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="会员等级 / 类型" min-width="160">
          <template #default="{ row }">
            <div v-if="row.memberLevel || row.vipType" class="flex items-center gap-1.5">
              <img v-if="row.vipConfigIcon" :src="row.vipConfigIcon" class="w-4 h-4 object-contain" />
              <span v-if="row.memberLevel" class="font-medium text-slate-700">{{ row.memberLevel }}</span>
              <el-tag v-if="row.vipType" size="small" type="info" class="ml-1">{{ row.vipType }}</el-tag>
            </div>
            <span v-else class="text-slate-400">-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ formatStatus(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="fans" label="粉丝" width="90" />
        <el-table-column prop="liked" label="获赞" width="90" />
        <el-table-column prop="topic" label="帖子数" width="90" />
        <el-table-column prop="ip" label="IP" min-width="140">
          <template #default="{ row }">
            {{ row.ip || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="注册时间" min-width="170">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <el-button
                v-if="row.status === 1 || row.status === '1'"
                size="small"
                type="danger"
                plain
                @click="handleToggleBan(row, true)"
              >
                封禁
              </el-button>
              <el-button
                v-else
                size="small"
                type="success"
                plain
                @click="handleToggleBan(row, false)"
              >
                解封
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="flex justify-end mt-6">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          class="admin-pagination"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </div>

    <el-dialog v-model="openFilter" title="筛选用户" width="760px">
      <div class="grid grid-cols-2 gap-4">
        <el-input v-model="filterForm.keyword" placeholder="昵称关键词" clearable />
        <el-input v-model="filterForm.phone" placeholder="手机号" clearable />

        <el-select v-model="filterForm.gender" placeholder="性别" clearable>
          <el-option label="男" value="1" />
          <el-option label="女" value="0" />
        </el-select>
        <el-select v-model="filterForm.statusCode" placeholder="状态" clearable>
          <el-option label="正常" :value="1" />
          <el-option label="封禁" :value="0" />
        </el-select>

        <el-input v-model="filterForm.ip" placeholder="IP 或归属地" clearable />
        <div class="flex items-center gap-3">
          <el-switch v-model="filterForm.enabledVIP" />
          <span class="text-sm text-slate-600">仅筛选 VIP 用户</span>
        </div>

        <div class="flex items-center gap-2">
          <el-input-number v-model="filterForm.age.min" :min="0" placeholder="最低年龄" class="w-full" />
          <span class="text-slate-400">-</span>
          <el-input-number v-model="filterForm.age.max" :min="0" placeholder="最高年龄" class="w-full" />
        </div>
        <div class="flex items-center gap-2">
          <el-input-number v-model="filterForm.VIPLevel.min" :min="0" placeholder="最低 VIP 等级" class="w-full" />
          <span class="text-slate-400">-</span>
          <el-input-number v-model="filterForm.VIPLevel.max" :min="0" placeholder="最高 VIP 等级" class="w-full" />
        </div>

        <div class="flex items-center gap-2">
          <el-input-number v-model="filterForm.fans.min" :min="0" placeholder="最低粉丝" class="w-full" />
          <span class="text-slate-400">-</span>
          <el-input-number v-model="filterForm.fans.max" :min="0" placeholder="最高粉丝" class="w-full" />
        </div>
        <div class="flex items-center gap-2">
          <el-input-number v-model="filterForm.follow.min" :min="0" placeholder="最低关注" class="w-full" />
          <span class="text-slate-400">-</span>
          <el-input-number v-model="filterForm.follow.max" :min="0" placeholder="最高关注" class="w-full" />
        </div>

        <div class="flex items-center gap-2 col-span-2">
          <el-input-number v-model="filterForm.topic.min" :min="0" placeholder="最低帖子数" class="w-full" />
          <span class="text-slate-400">-</span>
          <el-input-number v-model="filterForm.topic.max" :min="0" placeholder="最高帖子数" class="w-full" />
        </div>

        <el-date-picker
          v-model="filterForm.createTime"
          type="datetimerange"
          value-format="x"
          start-placeholder="注册开始时间"
          end-placeholder="注册结束时间"
          class="col-span-2"
        />
      </div>

      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="openFilter = false">取消</el-button>
          <el-button type="primary" @click="applyFilters">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ElMessageBox } from 'element-plus'
import { AdminUserAPI, type AdminUserQueryPayload, type RangeDTO } from '@/services/adminUser'
import type { UserProfileData } from '@/services/user'
import { onMounted, reactive, ref } from 'vue'

interface AdminUserFilterForm {
  keyword: string
  createTime: [string, string] | []
  enabledVIP: boolean
  VIPLevel: RangeDTO<number>
  fans: RangeDTO<number>
  topic: RangeDTO<number>
  follow: RangeDTO<number>
  age: RangeDTO<number>
  gender: string | null
  phone: string
  ip: string
  statusCode: number | null
}

const createDefaultFilterForm = (): AdminUserFilterForm => ({
  keyword: '',
  createTime: [],
  enabledVIP: false,
  VIPLevel: { min: null, max: null },
  fans: { min: null, max: null },
  topic: { min: null, max: null },
  follow: { min: null, max: null },
  age: { min: null, max: null },
  gender: null,
  phone: '',
  ip: '',
  statusCode: null,
})

const page = ref(1)
const pageSize = ref(10)
const openFilter = ref(false)

const filterForm = reactive<AdminUserFilterForm>(createDefaultFilterForm())
const list = ref<UserProfileData[]>([])
const loading = ref(false)
const total = ref(0)

const normalizeRange = <T>(range: RangeDTO<T>): RangeDTO<T> | null => {
  if (range.min == null && range.max == null) {
    return null
  }
  return {
    min: range.min,
    max: range.max,
  }
}

const buildPayload = (): AdminUserQueryPayload => {
  const createTime =
    filterForm.createTime.length === 2
      ? {
          min: new Date(Number(filterForm.createTime[0])).toISOString(),
          max: new Date(Number(filterForm.createTime[1])).toISOString(),
        }
      : null

  return {
    page: page.value,
    size: pageSize.value,
    keyword: filterForm.keyword.trim() || null,
    createTime,
    enabledVIP: filterForm.enabledVIP ? true : null,
    VIPLevel: normalizeRange(filterForm.VIPLevel),
    fans: normalizeRange(filterForm.fans),
    topic: normalizeRange(filterForm.topic),
    follow: normalizeRange(filterForm.follow),
    age: normalizeRange(filterForm.age),
    gender: filterForm.gender,
    phone: filterForm.phone.trim() || null,
    ip: filterForm.ip.trim() || null,
    statusCode: filterForm.statusCode,
  }
}

const formatGender = (gender?: string | number | null) => {
  if (gender === 1 || gender === '1') return '男'
  if (gender === 0 || gender === '0') return '女'
  return ''
}

const formatGenderAndAge = (gender?: string | number | null, age?: number | null) => {
  const genderText = formatGender(gender)
  const ageText = age != null ? `${age}岁` : ''
  if (genderText && ageText) {
    return `${genderText} / ${ageText}`
  }
  return genderText || ageText || '-'
}

const formatStatus = (status?: string | number | null) => {
  if (status === 1 || status === '1') return '正常'
  if (status === 0 || status === '0') return '已封禁'
  return '-'
}

const getStatusTagType = (status?: string | number | null) => {
  return status === 1 || status === '1' ? 'success' : 'danger'
}

const formatTime = (time?: string | null) => {
  if (!time) return '-'
  const date = new Date(time)
  return Number.isNaN(date.getTime()) ? time : date.toLocaleString('zh-CN', { timeZone: 'Asia/Shanghai' })
}

const query = async () => {
  loading.value = true
  try {
    const res = await AdminUserAPI.list(buildPayload())
    if (res.code === 1 && res.data) {
      list.value = res.data.data || (res.data as any).list || []
      total.value = Number(res.data.total) || 0
    }
  } catch {
    // 错误在 Axios 响应拦截器统一集中处理
  } finally {
    loading.value = false
  }
}

const handlePageChange = async (val: number) => {
  page.value = val
  await query()
}

const handleSizeChange = async (val: number) => {
  pageSize.value = val
  page.value = 1
  await query()
}

const applyFilters = async () => {
  page.value = 1
  openFilter.value = false
  await query()
}

const resetFilters = async () => {
  Object.assign(filterForm, createDefaultFilterForm())
  page.value = 1
  await query()
}

const handleToggleBan = async (row: UserProfileData, attemptBan: boolean) => {
  const actionText = attemptBan ? '封禁' : '解封'
  try {
    await ElMessageBox.confirm(`确定要${actionText}用户 [${row.nick || row.id}] 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    if (attemptBan) {
      await AdminUserAPI.ban(row.id)
    } else {
      await AdminUserAPI.unban(row.id)
    }
    await query()
  } catch (error: any) {
    if (error === 'cancel' || error === 'close') {
      return
    }
  }
}

onMounted(() => {
  query()
})
</script>

<style scoped>
.admin-pagination :deep(.el-pagination__total),
.admin-pagination :deep(.el-pagination__goto),
.admin-pagination :deep(.el-input__inner) {
  font-size: 13px;
  color: #64748b;
}

.admin-pagination :deep(button) {
  background-color: transparent !important;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.admin-pagination :deep(button:disabled) {
  background-color: #f8fafc !important;
  opacity: 0.5;
}

.admin-pagination :deep(.el-pager li) {
  background-color: transparent;
  border-radius: 8px;
  font-weight: 500;
  color: #64748b;
}

.admin-pagination :deep(.el-pager li.is-active) {
  background-color: #3b82f6 !important;
  color: #ffffff !important;
}
</style>
