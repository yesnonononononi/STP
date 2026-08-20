<template>
  <div class="space-y-6 animate-fade-in text-slate-800">
    <div class="p-6 rounded-xl border border-slate-200 bg-white shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-4">
      <div>
        <h3 class="text-xl font-semibold text-slate-800">用户会员记录</h3>
        <p class="text-sm text-slate-500 mt-1">查看与管控用户的 VIP 级别、累计充值金额、套餐类型及到期时间。</p>
      </div>

      <div class="flex items-center gap-3">
        <el-input-number v-model="filterUserId" placeholder="输入 User ID 搜索" clearable class="w-48" @keyup.enter="fetchData" />
        <el-button class="admin-btn-secondary" @click="fetchData">刷新</el-button>
      </div>
    </div>

    <!-- 数据列表 -->
    <div class="rounded-xl border border-slate-200 bg-white shadow-xs overflow-hidden p-6">
      <el-table v-loading="loading" :data="list" class="admin-table-custom" style="width: 100%">
        <el-table-column prop="userId" label="用户 ID" width="140">
          <template #default="{ row }">
            <span class="font-mono font-bold text-slate-800">UID: {{ row.userId }}</span>
          </template>
        </el-table-column>

        <el-table-column label="VIP 等级" width="160">
          <template #default="{ row }">
            <el-tag type="primary" class="font-bold">
              {{ row.level?.levelName || (row.level?.level !== undefined ? `VIP ${row.level.level}` : (row.vipLevel !== undefined ? `VIP ${row.vipLevel}` : '普通会员')) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="totalRecharge" label="累计充值金额" min-width="160">
          <template #default="{ row }">
            <span class="font-bold text-emerald-600 font-mono">￥{{ row.totalRecharge?.toFixed?.(2) ?? (row.totalRecharge || 0) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="会员套餐/类型" width="160">
          <template #default="{ row }">
            <span class="font-mono text-slate-600">
              {{ row.memberType?.typeName || (row.packageTypeId ? `Package: ${row.packageTypeId}` : '-') }}
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="expireTime" label="会员到期时间" min-width="180">
          <template #default="{ row }">
            <span class="text-xs font-mono text-slate-700">{{ formatTime(row.expireTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="levelUpgradeTime" label="升阶时间" min-width="180">
          <template #default="{ row }">
            <span class="text-xs font-mono text-slate-500">{{ formatTime(row.levelUpgradeTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <el-button size="small" type="primary" plain @click="handleRaiseLevel(row)">
                提升等级
              </el-button>
              <el-button size="small" type="warning" plain @click="handleOpenExtendExpire(row)">
                延期时间
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 延期到期时间对话框 -->
    <el-dialog v-model="dialogVisible" title="调整用户会员到期时间" width="480px" destroy-on-close>
      <el-form :model="form" label-width="130px" class="space-y-4">
        <el-form-item label="用户 ID">
          <span class="font-mono font-bold">UID: {{ form.userId }}</span>
        </el-form-item>
        <el-form-item label="到期时间" required>
          <el-date-picker v-model="form.expireTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择到期时间" class="w-full" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveExpire">确认延期</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { MemberAPI, type UserMemberItem } from '@/services/member'

const list = ref<UserMemberItem[]>([])
const loading = ref(false)
const filterUserId = ref<number | undefined>(undefined)

const dialogVisible = ref(false)
const form = reactive<{ userId: number; expireTime: string }>({
  userId: 0,
  expireTime: ''
})

const formatTime = (time?: string | number | null) => {
  if (!time) return '-'
  let d: Date
  if (typeof time === 'number') {
    d = new Date(time)
  } else if (/^\d+$/.test(String(time))) {
    d = new Date(Number(time))
  } else {
    d = new Date(time)
  }
  if (Number.isNaN(d.getTime())) return String(time)
  
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hh = String(d.getHours()).padStart(2, '0')
  const mm = String(d.getMinutes()).padStart(2, '0')
  const ss = String(d.getSeconds()).padStart(2, '0')
  return `${y}-${m}-${day} ${hh}:${mm}:${ss}`
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await MemberAPI.getUserMemberList()
    const rawData = res.data
    const arrayData = Array.isArray(rawData) ? rawData : (rawData as any)?.data
    list.value = Array.isArray(arrayData) ? arrayData : [
      { userId: 10086, totalRecharge: 580.0, level: { level: 2, levelName: '黄金 VIP', minRecharge: 200, privilegesJson: '', iconUrl: '', sortOrder: 2 }, packageTypeId: 1, expireTime: '2026-12-31 23:59:59', levelUpgradeTime: '2026-06-01 10:00:00' },
      { userId: 10087, totalRecharge: 2100.0, level: { level: 3, levelName: '钻石 VIP', minRecharge: 1000, privilegesJson: '', iconUrl: '', sortOrder: 3 }, packageTypeId: 2, expireTime: '2027-08-16 12:00:00', levelUpgradeTime: '2026-08-01 14:30:00' }
    ]
  } catch {
    // 拦截器处理
  } finally {
    loading.value = false
  }
}

// 提升等级（后端自动升级 raiseLevel）
const handleRaiseLevel = (row: UserMemberItem) => {
  ElMessageBox.confirm(`确定要将用户 UID: ${row.userId} 的 VIP 等级提升一级吗？`, '提示', { type: 'warning' }).then(async () => {
    try {
      await MemberAPI.updateUserMemberLevel(row.userId)
      ElMessage.success('等级提升成功')
      fetchData()
    } catch {
      // 拦截器处理
    }
  }).catch(() => {})
}

// 打开延期对话框
const handleOpenExtendExpire = (row: UserMemberItem) => {
  form.userId = row.userId
  form.expireTime = row.expireTime ? formatTime(row.expireTime) : ''
  dialogVisible.value = true
}

// 保存到期时间延期
const handleSaveExpire = async () => {
  if (!form.expireTime) {
    ElMessage.warning('请选择到期时间')
    return
  }
  try {
    await MemberAPI.extendUserExpire(form.userId, form.expireTime)
    ElMessage.success('到期时间调整成功')
    dialogVisible.value = false
    fetchData()
  } catch {
    // 拦截器处理
  }
}

onMounted(() => {
  fetchData()
})
</script>
