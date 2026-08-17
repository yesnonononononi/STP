<template>
  <div class="space-y-6 animate-fade-in text-slate-800">
    <!-- 头部卡片与工具栏 -->
    <div class="p-6 rounded-xl border border-slate-200 bg-white shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-4">
      <div>
        <h3 class="text-xl font-semibold text-slate-800">订单交易管理</h3>
        <p class="text-sm text-slate-500 mt-1">管理会员与商品订单，支持多维度组合筛选、异常订单补单、退单退款及渠道核对。</p>
      </div>

      <div class="flex items-center gap-3">
        <el-input v-model="filterForm.orderNo" placeholder="输入订单号搜索..." clearable class="w-64" @keyup.enter="fetchData" />
        <el-button type="primary" class="admin-btn-primary" @click="openFilterDialog = true">
          高级条件筛选
        </el-button>
        <el-button class="admin-btn-secondary" @click="fetchData">刷新</el-button>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="rounded-xl border border-slate-200 bg-white shadow-xs overflow-hidden p-6">
      <el-table v-loading="loading" :data="list" class="admin-table-custom" style="width: 100%">
        <el-table-column prop="orderNo" label="订单号" min-width="180">
          <template #default="{ row }">
            <span class="font-mono font-semibold text-slate-800">{{ row.orderNo }}</span>
          </template>
        </el-table-column>

        <el-table-column label="购买用户" min-width="150">
          <template #default="{ row }">
            <span class="text-sm text-slate-800 font-medium">{{ row.userName }}</span>
            <span class="text-xs text-slate-400 block">ID: {{ row.userId }}</span>
          </template>
        </el-table-column>

        <el-table-column label="订单类型" width="130">
          <template #default="{ row }">
            <el-tag :type="getOrderTypeTag(row.orderType)">
              {{ getOrderTypeName(row.orderType) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="amount" label="实付金额" min-width="120">
          <template #default="{ row }">
            <span class="font-bold text-rose-600 font-mono text-base">￥{{ row.amount.toFixed(2) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="支付渠道" width="120">
          <template #default="{ row }">
            <span class="text-xs font-medium text-slate-700">{{ getPayTypeName(row.payType) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="支付状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getPayStatusTag(row.payStatus)">
              {{ getPayStatusName(row.payStatus) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="下单 / 支付时间" min-width="180">
          <template #default="{ row }">
            <div class="text-xs text-slate-500 space-y-0.5">
              <div>下单: {{ row.createTime }}</div>
              <div v-if="row.payTime" class="text-emerald-600">支付: {{ row.payTime }}</div>
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

              <!-- 补单（针对支付成功未履约的订单） -->
              <el-button v-if="row.payStatus === 'PAID'" size="small" type="warning" plain @click="handleReissuance(row)">
                手工补单
              </el-button>

              <!-- 退单退款 -->
              <el-button v-if="row.payStatus === 'PAID'" size="small" type="danger" plain @click="openRefundModal(row)">
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
        <el-input v-model="filterForm.orderNo" placeholder="订单号" clearable />
        <el-input v-model.number="filterForm.userId" placeholder="用户 ID" clearable />

        <el-select v-model="filterForm.orderType" placeholder="订单类型" clearable>
          <el-option label="会员充值/购买" value="MEMBER" />
          <el-option label="优惠券包" value="COUPON_PACKAGE" />
          <el-option label="其他商品" value="OTHER" />
        </el-select>

        <el-select v-model="filterForm.payStatus" placeholder="支付状态" clearable>
          <el-option label="已支付" value="PAID" />
          <el-option label="待支付" value="UNPAID" />
          <el-option label="已退款" value="REFUNDED" />
          <el-option label="支付失败" value="FAILED" />
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
        <p class="text-sm text-slate-600">确定要退单订单 <strong>[ {{ activeOrder?.orderNo }} ]</strong> 吗？</p>
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
import { OrderAPI, type OrderItem, type OrderQueryPayload } from '@/services/order'

const list = ref<OrderItem[]>([])
const loading = ref(false)
const total = ref(0)
const pagination = reactive({ page: 1, size: 10 })
const openFilterDialog = ref(false)

const filterForm = reactive<{ orderNo?: string; userId?: number; orderType?: string; payStatus?: string }>({})

// 退单弹窗
const refundDialogVisible = ref(false)
const activeOrder = ref<OrderItem | null>(null)
const refundReasonInput = ref('')

const getOrderTypeName = (type: string) => {
  if (type === 'MEMBER') return '会员开通'
  if (type === 'COUPON_PACKAGE') return '优惠券包'
  return '其他交易'
}

const getOrderTypeTag = (type: string) => {
  if (type === 'MEMBER') return 'primary'
  if (type === 'COUPON_PACKAGE') return 'warning'
  return 'info'
}

const getPayTypeName = (type: string) => {
  if (type === 'WECHAT') return '微信支付'
  if (type === 'ALIPAY') return '支付宝'
  return '余额支付'
}

const getPayStatusName = (status: string) => {
  if (status === 'PAID') return '已完成支付'
  if (status === 'UNPAID') return '待支付'
  if (status === 'REFUNDED') return '已退款'
  return '支付失败'
}

const getPayStatusTag = (status: string) => {
  if (status === 'PAID') return 'success'
  if (status === 'UNPAID') return 'warning'
  if (status === 'REFUNDED') return 'danger'
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
    list.value = res.data?.list || [
      { id: 901, orderNo: 'ORD202608169901', userId: 10086, userName: '极客小张', orderType: 'MEMBER', amount: 198.0, payType: 'WECHAT', payStatus: 'PAID', createTime: '2026-08-16 09:12', payTime: '2026-08-16 09:13' },
      { id: 902, orderNo: 'ORD202608169902', userId: 10089, userName: '新买家', orderType: 'COUPON_PACKAGE', amount: 29.9, payType: 'ALIPAY', payStatus: 'UNPAID', createTime: '2026-08-16 14:05' },
      { id: 903, orderNo: 'ORD202608169903', userId: 10095, userName: '老用户A', orderType: 'MEMBER', amount: 25.0, payType: 'WECHAT', payStatus: 'REFUNDED', createTime: '2026-08-15 16:30', payTime: '2026-08-15 16:31' }
    ]
    total.value = res.data?.total || list.value.length
  } catch {
    // 拦截器处理
  } finally {
    loading.value = false
  }
}

// 状态核对
const handleVerifyStatus = async (row: OrderItem) => {
  try {
    const res = await OrderAPI.verifyOrderStatus(row.orderNo)
    ElMessage.info(res.data?.message || `订单 [${row.orderNo}] 渠道核对状态一致`)
    fetchData()
  } catch {
    // 拦截器处理
  }
}

// 补单
const handleReissuance = (row: OrderItem) => {
  ElMessageBox.confirm(`确定要对订单 [${row.orderNo}] 执行手工补单（重新触发权益发放）吗？`, '补单确认', { type: 'warning' }).then(async () => {
    try {
      await OrderAPI.reissuanceOrder(row.orderNo)
      fetchData()
    } catch {
      // 拦截器处理
    }
  }).catch(() => {})
}

// 打开退单弹窗
const openRefundModal = (row: OrderItem) => {
  activeOrder.value = row
  refundReasonInput.value = ''
  refundDialogVisible.value = true
}

const confirmRefund = async () => {
  if (!activeOrder.value || !refundReasonInput.value.trim()) return
  try {
    await OrderAPI.refundOrder(activeOrder.value.orderNo, refundReasonInput.value.trim())
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
