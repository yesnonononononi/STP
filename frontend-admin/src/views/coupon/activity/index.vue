<template>
  <div class="space-y-6 animate-fade-in text-slate-800">
    <div class="p-6 rounded-xl border border-slate-200 bg-white shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-4">
      <div>
        <h3 class="text-xl font-semibold text-slate-800">营销活动管理</h3>
        <p class="text-sm text-slate-500 mt-1">创建并运营优惠券营销活动（系统规则统一为一人仅限领一张）。</p>
      </div>

      <div class="flex items-center gap-3">
        <el-input v-model="keyword" placeholder="搜索活动名称..." clearable class="w-64" @keyup.enter="fetchData" />
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

        <el-table-column prop="name" label="活动名称" min-width="180">
          <template #default="{ row }">
            <div class="font-semibold text-slate-800">{{ row.name }}</div>
          </template>
        </el-table-column>

        <el-table-column label="活动类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.type === 2 ? 'danger' : 'primary'" effect="plain">
              {{ row.type === 2 ? '秒杀活动' : '普通活动' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="关联优惠券" min-width="140">
          <template #default="{ row }">
            <el-tag type="warning" plain>优惠券 ID: {{ row.couponId }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="库存数量" min-width="120">
          <template #default="{ row }">
            <span class="font-mono font-medium text-slate-800">{{ row.stock ?? 0 }}</span>
          </template>
        </el-table-column>

        <el-table-column label="活动有效时间" min-width="220">
          <template #default="{ row }">
            <div class="text-xs text-slate-600 space-y-0.5">
              <div><span class="text-slate-400">始:</span> {{ formatTime(row.activityStartTime) }}</div>
              <div><span class="text-slate-400">终:</span> {{ formatTime(row.activityEndTime) }}</div>
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
              <el-button size="small" type="primary" plain @click="handleEdit(row)">
                编辑
              </el-button>
              <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" plain @click="handleToggleStatus(row)">
                {{ row.status === 1 ? '关闭' : '开启' }}
              </el-button>
              <el-button size="small" type="danger" text @click="handleDelete(row)">
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="flex justify-end mt-6">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="fetchData"
          @size-change="fetchData"
        />
      </div>
    </div>

    <!-- 创建 / 编辑活动对话框 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑营销活动' : '创建营销活动'" width="560px" destroy-on-close>
      <el-form :model="form" label-width="120px" class="space-y-4">
        <el-form-item label="活动名称" required>
          <el-input v-model="form.name" placeholder="如：开学季狂欢领券" />
        </el-form-item>

        <el-form-item label="活动类型" required>
          <el-select v-model="form.type" class="w-full">
            <el-option label="普通活动" :value="1" />
            <el-option label="秒杀活动" :value="2" />
          </el-select>
        </el-form-item>

        <el-form-item label="关联优惠券 ID" required>
          <el-input-number v-model="form.couponId" :min="1" class="w-full" />
        </el-form-item>

        <el-form-item label="活动库存总量" required>
          <el-input-number v-model="form.stock" :min="1" class="w-full" />
        </el-form-item>

        <el-form-item label="运行状态" required>
          <el-select v-model="form.status" class="w-full">
            <el-option label="开启 / 进行中" :value="1" />
            <el-option label="未开启 / 关闭" :value="0" />
          </el-select>
        </el-form-item>

        <el-form-item label="活动开始时间">
          <el-date-picker v-model="form.activityStartTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择开始时间" class="w-full" clearable />
        </el-form-item>

        <el-form-item label="活动结束时间">
          <el-date-picker v-model="form.activityEndTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择结束时间" class="w-full" clearable />
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
import { ElMessageBox, ElMessage } from 'element-plus'
import { CouponAPI, type AdminCouponActivityVO, type CreateCouponActivityPayload } from '@/services/coupon'

const list = ref<AdminCouponActivityVO[]>([])
const loading = ref(false)
const keyword = ref('')
const total = ref(0)
const pagination = reactive({ page: 1, pageSize: 10 })
const dialogVisible = ref(false)

// 使用 ref 容器维持表单项在编辑、重置与切换时的强响应绑定
const form = ref<CreateCouponActivityPayload>({
  id: undefined,
  name: '',
  type: 1,
  couponId: 1,
  stock: 100,
  status: 1,
  activityStartTime: undefined,
  activityEndTime: undefined
})

const getStatusName = (status?: number) => {
  if (status === 1) return '开启/进行中'
  if (status === 0) return '已关闭/暂停'
  return '未开启'
}

const getStatusTag = (status?: number) => {
  if (status === 1) return 'success'
  if (status === 0) return 'warning'
  return 'info'
}

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
    const res = await CouponAPI.getActivityList({
      keyword: keyword.value.trim() || undefined,
      page: pagination.page,
      pageSize: pagination.pageSize
    })
    if (res.code === 1 && res.data) {
      list.value = res.data.data || []
      total.value = Number(res.data.total) || 0
    }
  } catch {
    // 拦截器处理
  } finally {
    loading.value = false
  }
}

// 打开创建活动弹窗 (重置 form.value)
const handleOpenAdd = () => {
  form.value = {
    id: undefined,
    name: '',
    type: 1,
    couponId: 1,
    stock: 100,
    status: 1,
    activityStartTime: undefined,
    activityEndTime: undefined
  }
  dialogVisible.value = true
}

// 打开编辑活动弹窗 (精准回显与类型规整，确保数值为 Number)
const handleEdit = (row: AdminCouponActivityVO) => {
  form.value = {
    id: row.id != null ? Number(row.id) : undefined,
    name: row.name || '',
    type: row.type != null ? Number(row.type) : 1,
    couponId: row.couponId != null ? Number(row.couponId) : 1,
    stock: row.stock != null ? Number(row.stock) : 100,
    status: row.status != null ? Number(row.status) : 1,
    activityStartTime: row.activityStartTime ? formatTime(row.activityStartTime) : undefined,
    activityEndTime: row.activityEndTime ? formatTime(row.activityEndTime) : undefined
  }
  dialogVisible.value = true
}

// 保存逻辑：直接根据 form.value 获取用户界面实时输入的最新数据
const handleSave = async () => {
  if (!form.value.name.trim()) {
    ElMessage.warning('请输入活动名称')
    return
  }
  try {
    const payload: CreateCouponActivityPayload = {
      id: form.value.id != null ? Number(form.value.id) : undefined,
      name: form.value.name.trim(),
      type: Number(form.value.type ?? 1),
      couponId: Number(form.value.couponId),
      stock: Number(form.value.stock),
      status: Number(form.value.status ?? 1),
      activityStartTime: form.value.activityStartTime && String(form.value.activityStartTime).trim() !== '' ? form.value.activityStartTime : undefined,
      activityEndTime: form.value.activityEndTime && String(form.value.activityEndTime).trim() !== '' ? form.value.activityEndTime : undefined,
    }
    await CouponAPI.saveActivity(payload)
    dialogVisible.value = false
    fetchData()
  } catch {
    // 拦截器处理
  }
}

const handleToggleStatus = async (row: AdminCouponActivityVO) => {
  const isStart = row.status !== 1
  const actionText = isStart ? '开启' : '关闭'
  ElMessageBox.confirm(`确定要${actionText}活动 [${row.name}] 吗？`, '提示', { type: 'warning' }).then(async () => {
    try {
      if (isStart) {
        await CouponAPI.startActivity(row.id)
      } else {
        await CouponAPI.closeActivity(row.id)
      }
      fetchData()
    } catch {
      // 拦截器处理
    }
  }).catch(() => {})
}

// 删除活动 (仅允许在活动结束之后或处于开始时间之前进行删除)
const handleDelete = (row: AdminCouponActivityVO) => {
  const now = new Date().getTime()
  const start = row.activityStartTime ? new Date(row.activityStartTime).getTime() : null
  const end = row.activityEndTime ? new Date(row.activityEndTime).getTime() : null

  // 如果处于活动时间段内且状态开启，提前进行友情提示拦截
  if (start && end && now >= start && now <= end && row.status === 1) {
    ElMessage.warning('活动正在进行中，不可删除！')
    return
  }

  ElMessageBox.confirm(`确定要彻底删除活动 [${row.name}] 吗？`, '删除确认', { type: 'error' }).then(async () => {
    try {
      await CouponAPI.deleteActivity(row.id)
      fetchData()
    } catch {
      // 拦截器捕获后端校验提示
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchData()
})
</script>
