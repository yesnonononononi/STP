<template>
  <div class="space-y-6 animate-fade-in text-slate-800">
    <!-- 头部卡片与工具栏 -->
    <div class="p-6 rounded-xl border border-slate-200 bg-white shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-4">
      <div>
        <h3 class="text-xl font-semibold text-slate-800">订单交易管理</h3>
        <p class="text-sm text-slate-500 mt-1">管理会员与商品订单，支持多维度组合筛选、异常订单补单、退单退款及渠道核对。</p>
      </div>

      <div class="flex items-center gap-3">
        <el-input v-model="filterForm.orderNo" placeholder="输入订单 ID 搜索..." clearable class="w-64" @keyup.enter="fetchData" />
        <el-button type="primary" class="admin-btn-primary" @click="openFilterDialog = true">
          高级条件筛选
        </el-button>
        <el-button class="admin-btn-secondary" @click="fetchData">刷新</el-button>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="rounded-xl border border-slate-200 bg-white shadow-xs overflow-hidden p-6">
      <el-table v-loading="loading" :data="list" class="admin-table-custom" style="width: 100%">
        <el-table-column prop="id" label="订单 ID" min-width="170">
          <template #default="{ row }">
            <span class="font-mono font-semibold text-slate-800">{{ row.id }}</span>
          </template>
        </el-table-column>

        <el-table-column label="购买用户" min-width="140">
          <template #default="{ row }">
            <span class="font-mono font-medium text-slate-800">UID: {{ row.creatorId }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="amount" label="实付金额" min-width="120">
          <template #default="{ row }">
            <span class="font-bold text-rose-600 font-mono text-base">￥{{ row.amount?.toFixed?.(2) ?? row.amount }}</span>
          </template>
        </el-table-column>

        <el-table-column label="支付渠道" width="120">
          <template #default="{ row }">
            <span class="text-xs font-medium text-slate-700">{{ getPayTypeName(row.payType) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="支付状态" width="130">
          <template #default="{ row }">
            <el-tag :type="getPayStatusTag(row.status)">
              {{ getPayStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="下单 / 支付时间" min-width="180">
          <template #default="{ row }">
            <div class="text-xs text-slate-500 space-y-0.5">
              <div>下单: {{ formatDate(row.createTime) }}</div>
              <div v-if="row.payTime" class="text-emerald-600">支付: {{ formatDate(row.payTime) }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作与核对" width="280" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <!-- 订单状态核对 -->
              <el-button size="small" type="primary" plain @click="handleVerifyStatus(row)">
                状态核对
              </el-button>

              <!-- 补单 -->
              <el-button v-if="row.status === 1" size="small" type="warning" plain @click="handleReissuance(row)">
                手工补单
              </el-button>

              <!-- 退单退款 -->
              <el-button v-if="row.status === 1" size="small" type="danger" plain @click="openRefundModal(row)">
                发起退单
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页栏 -->
      <div class="flex justify-end mt-6">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="fetchData"
          @size-change="fetchData"
        />
      </div>
    </div>

    <!-- 高级条件筛选弹窗 -->
    <el-dialog v-model="openFilterDialog" title="多条件筛选订单" width="640px">
      <div class="grid grid-cols-2 gap-4">
        <el-input v-model="filterForm.orderNo" placeholder="订单 ID" clearable />
        <el-input v-model.number="filterForm.userId" placeholder="用户 ID" clearable />

        <el-select v-model="filterForm.payStatus" placeholder="支付状态" clearable>
          <el-option label="待支付" :value="0" />
          <el-option label="已支付" :value="1" />
          <el-option label="已完成" :value="2" />
          <el-option label="已取消" :value="3" />
        </el-select>
      </div>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="resetFilter">重置</el-button>
          <el-button type="primary" @click="applyFilter">应用筛选</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 退单理由弹窗 -->
    <el-dialog v-model="refundDialogVisible" title="发起退单退款" width="460px">
      <div class="space-y-3">
        <p class="text-sm text-slate-600">确定要退单订单 <strong>[ {{ activeOrder?.id }} ]</strong> 吗？</p>
        <el-input v-model="refundReasonInput" type="textarea" :rows="3" placeholder="请输入退单原由或退款备注..." />
      </div>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="refundDialogVisible = false">取消</el-button>
          <el-button type="danger" :disabled="!refundReasonInput.trim()" @click="confirmRefund">
            确认退单
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { OrderAPI, type AdminOrderVO, type OrderQueryPayload } from '@/services/order'
import { formatDate } from '@/utils/formatDate'

const list = ref<AdminOrderVO[]>([])
const loading = ref(false)
const total = ref(0)
const pagination = reactive({ page: 1, size: 10 })
const openFilterDialog = ref(false)

const filterForm = reactive<{ orderNo?: string; userId?: number; orderType?: number; payStatus?: number }>({})

// 退单弹窗
const refundDialogVisible = ref(false)
const activeOrder = ref<AdminOrderVO | null>(null)
const refundReasonInput = ref('')

const getPayTypeName = (type?: number) => {
  if (type === 1) return '微信支付'
  if (type === 2) return '支付宝'
  if (type === 3) return '余额支付'
  return '未指定渠道'
}

const getPayStatusName = (status?: number | string) => {
  const s = status !== undefined && status !== null ? String(status) : ''
  if (s === '0' || s === 'PENDING') return '待支付'
  if (s === '1' || s === 'PAID') return '已支付'
  if (s === '2' || s === 'COMPLETED') return '已完成'
  if (s === '3' || s === 'CANCELLED') return '已取消'
  return status !== undefined && status !== null ? `状态: ${status}` : '-'
}

const getPayStatusTag = (status?: number | string) => {
  const s = status !== undefined && status !== null ? String(status) : ''
  if (s === '1' || s === 'PAID') return 'primary'
  if (s === '2' || s === 'COMPLETED') return 'success'
  if (s === '0' || s === 'PENDING') return 'warning'
  if (s === '3' || s === 'CANCELLED') return 'info'
  return 'info'
}

const fetchData = async () => {
  loading.value = true
  try {
    const payload: OrderQueryPayload = {
      page: pagination.page,
      size: pagination.size,
      ...filterForm
    }
    const res = await OrderAPI.getOrderList(payload)
    if (res && res.data) {
      list.value = res.data.data || []
      total.value = Number(res.data.total) || 0
    }
  } catch {
    // 拦截器处理
  } finally {
    loading.value = false
  }
}

// 状态核对
const handleVerifyStatus = async (row: AdminOrderVO) => {
  try {
    const res = await OrderAPI.verifyOrderStatus(row.id)
    ElMessage.info(res.data || `订单 [${row.id}] 渠道核对完成`)
    fetchData()
  } catch {
    // 拦截器处理
  }
}

// 补单
const handleReissuance = (row: AdminOrderVO) => {
  ElMessageBox.confirm(`确定要对订单 [${row.id}] 执行手工补单（重新触发权益发放）吗？`, '补单确认', { type: 'warning' }).then(async () => {
    try {
      await OrderAPI.reissuanceOrder(row.id)
      ElMessage.success('补单指令已发送')
      fetchData()
    } catch {
      // 拦截器处理
    }
  }).catch(() => {})
}

// 打开退单弹窗
const openRefundModal = (row: AdminOrderVO) => {
  activeOrder.value = row
  refundReasonInput.value = ''
  refundDialogVisible.value = true
}

const confirmRefund = async () => {
  if (!activeOrder.value || !refundReasonInput.value.trim()) return
  try {
    await OrderAPI.refundOrder(activeOrder.value.id, refundReasonInput.value.trim())
    ElMessage.success('退单退款申请提交成功')
    refundDialogVisible.value = false
    fetchData()
  } catch {
    // 拦截器处理
  }
}

const applyFilter = () => {
  pagination.page = 1
  openFilterDialog.value = false
  fetchData()
}

const resetFilter = () => {
  Object.keys(filterForm).forEach(k => delete (filterForm as any)[k])
  pagination.page = 1
  openFilterDialog.value = false
  fetchData()
}

onMounted(() => {
  fetchData()
})
</script>
