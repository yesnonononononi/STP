<template>
  <div class="space-y-6 animate-fade-in text-slate-800">
    <div class="p-6 rounded-xl border border-slate-200 bg-white shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-4">
      <div>
        <h3 class="text-xl font-semibold text-slate-800">营销活动管理</h3>
        <p class="text-sm text-slate-500 mt-1">创建并运营优惠券营销活动，配置活动开放周期及开启/暂停状态。</p>
      </div>

      <div class="flex items-center gap-3">
        <el-button type="primary" class="admin-btn-primary" @click="handleOpenAdd">
          + 创建活动
        </el-button>
        <el-button class="admin-btn-secondary" @click="fetchData">刷新</el-button>
      </div>
    </div>

    <!-- 数据列表卡片 -->
    <div class="rounded-xl border border-slate-200 bg-white shadow-xs overflow-hidden p-6">
      <el-table v-loading="loading" :data="list" class="admin-table-custom" style="width: 100%">
        <el-table-column prop="id" label="活动 ID" width="90" />

        <el-table-column prop="activityName" label="活动名称" min-width="180">
          <template #default="{ row }">
            <div class="font-semibold text-slate-800">{{ row.activityName }}</div>
            <div class="text-xs text-slate-400 truncate max-w-xs">{{ row.description }}</div>
          </template>
        </el-table-column>

        <el-table-column label="关联优惠券" min-width="160">
          <template #default="{ row }">
            <el-tag type="warning" plain>{{ row.associatedCouponTitle || `券 ID: ${row.associatedCouponId}` }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="活动有效时间" min-width="220">
          <template #default="{ row }">
            <div class="text-xs text-slate-600 space-y-0.5">
              <div><span class="text-slate-400">始:</span> {{ row.startTime }}</div>
              <div><span class="text-slate-400">终:</span> {{ row.endTime }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="运行状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <el-button size="small" type="primary" plain @click="handleEdit(row)">编辑</el-button>
              <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" plain @click="handleToggleStatus(row)">
                {{ row.status === 1 ? '暂停活动' : '开启活动' }}
              </el-button>
              <el-button size="small" type="danger" text @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 对话框 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑营销活动' : '创建营销活动'" width="560px">
      <el-form :model="form" label-width="110px" class="space-y-4">
        <el-form-item label="活动名称" required>
          <el-input v-model="form.activityName" placeholder="如：2026开学季狂欢领券" />
        </el-form-item>
        <el-form-item label="关联优惠券 ID" required>
          <el-input-number v-model="form.associatedCouponId" :min="1" class="w-full" />
        </el-form-item>
        <el-form-item label="活动描述">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="详细活动规则..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSave">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessageBox } from 'element-plus'
import { CouponAPI, type CouponActivityItem } from '@/services/coupon'

const list = ref<CouponActivityItem[]>([])
const loading = ref(false)
const dialogVisible = ref(false)

const form = reactive<Partial<CouponActivityItem>>({
  id: undefined,
  activityName: '',
  description: '',
  associatedCouponId: 1001,
  status: 1
})

const getStatusName = (status: number) => {
  if (status === 1) return '进行中'
  if (status === 0) return '已暂停'
  return '已结束'
}

const getStatusTag = (status: number) => {
  if (status === 1) return 'success'
  if (status === 0) return 'warning'
  return 'info'
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await CouponAPI.getActivityList()
    list.value = res.data?.list || [
      { id: 201, activityName: '暑期冲浪季·满减券大发放', description: '全员可领，限时3天', associatedCouponId: 1001, associatedCouponTitle: '双11全场无门槛立减券', status: 1, startTime: '2026-08-01 00:00', endTime: '2026-08-31 23:59', createTime: '2026-07-30' },
      { id: 202, activityName: '新会员首单专属优惠活动', description: '新注册会员自动发放', associatedCouponId: 1002, associatedCouponTitle: 'VIP会员专享8.8折优惠券', status: 0, startTime: '2026-01-01 00:00', endTime: '2026-12-31 23:59', createTime: '2026-01-01' }
    ]
  } catch {
    // 拦截器处理
  } finally {
    loading.value = false
  }
}

const handleOpenAdd = () => {
  Object.assign(form, { id: undefined, activityName: '', description: '', associatedCouponId: 1001, status: 1 })
  dialogVisible.value = true
}

const handleEdit = (row: CouponActivityItem) => {
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSave = async () => {
  try {
    await CouponAPI.saveActivity(form)
    dialogVisible.value = false
    fetchData()
  } catch {
    // 拦截器处理
  }
}

const handleToggleStatus = async (row: CouponActivityItem) => {
  const nextStatus = row.status === 1 ? 0 : 1
  const actionText = nextStatus === 0 ? '暂停' : '开启'
  ElMessageBox.confirm(`确定要${actionText}活动 [${row.activityName}] 吗？`, '提示', { type: 'warning' }).then(async () => {
    try {
      await CouponAPI.toggleActivityStatus(row.id, nextStatus)
      fetchData()
    } catch {
      // 拦截器处理
    }
  }).catch(() => {})
}

const handleDelete = (row: CouponActivityItem) => {
  ElMessageBox.confirm(`确定要删除活动 [${row.activityName}] 吗？`, '删除确认', { type: 'error' }).then(async () => {
    try {
      await CouponAPI.deleteActivity(row.id)
      fetchData()
    } catch {
      // 拦截器处理
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchData()
})
</script>
