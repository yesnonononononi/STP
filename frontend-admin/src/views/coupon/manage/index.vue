<template>
  <div class="space-y-6 animate-fade-in text-slate-800">
    <div class="p-6 rounded-xl border border-slate-200 bg-white shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-4">
      <div>
        <h3 class="text-xl font-semibold text-slate-800">优惠券管理</h3>
        <p class="text-sm text-slate-500 mt-1">创建立减券/折扣券，管控发放库存与封禁解封状态。</p>
      </div>

      <div class="flex items-center gap-3">
        <el-input v-model="keyword" placeholder="搜索优惠券标题..." clearable class="w-64" />
        <el-button type="primary" class="admin-btn-primary" @click="handleOpenAdd">
          + 新增优惠券
        </el-button>
        <el-button class="admin-btn-secondary" @click="fetchData">刷新</el-button>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="rounded-xl border border-slate-200 bg-white shadow-xs overflow-hidden p-6">
      <el-table v-loading="loading" :data="list" class="admin-table-custom" style="width: 100%">
        <el-table-column prop="id" label="券 ID" width="90" />

        <el-table-column prop="title" label="优惠券标题" min-width="160">
          <template #default="{ row }">
            <span class="font-semibold text-slate-800">{{ row.title }}</span>
          </template>
        </el-table-column>

        <el-table-column label="优惠类型 / 面额" min-width="150">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <el-tag :type="row.couponType === 'CASH' ? 'danger' : 'warning'" effect="plain">
                {{ row.couponType === 'CASH' ? '满减券' : '折扣券' }}
              </el-tag>
              <span class="font-bold text-rose-600">
                {{ row.couponType === 'CASH' ? `￥${row.discountValue}` : `${row.discountValue} 折` }}
              </span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="minThreshold" label="使用门槛" min-width="120">
          <template #default="{ row }">
            <span>{{ row.minThreshold > 0 ? `满 ￥${row.minThreshold}` : '无门槛' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="发放 / 剩余库存" min-width="160">
          <template #default="{ row }">
            <div class="space-y-1">
              <div class="flex justify-between text-xs text-slate-500">
                <span>剩余 {{ row.remainCount }}</span>
                <span>总量 {{ row.totalCount }}</span>
              </div>
              <el-progress :percentage="Math.round((row.remainCount / row.totalCount) * 100)" :show-text="false" status="warning" />
            </div>
          </template>
        </el-table-column>

        <el-table-column label="有效期限" min-width="200">
          <template #default="{ row }">
            <div class="text-xs text-slate-500">
              <div>自: {{ row.validStartTime }}</div>
              <div>至: {{ row.validEndTime }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '已封禁' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <el-button size="small" type="primary" plain @click="handleEdit(row)">编辑</el-button>
              <el-button size="small" :type="row.status === 1 ? 'danger' : 'success'" plain @click="handleToggleStatus(row)">
                {{ row.status === 1 ? '封禁' : '解封' }}
              </el-button>
              <el-button size="small" type="danger" text @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 对话框 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑优惠券' : '新增优惠券'" width="560px">
      <el-form :model="form" label-width="110px" class="space-y-4">
        <el-form-item label="优惠券标题" required>
          <el-input v-model="form.title" placeholder="如：新人满100减20专享券" />
        </el-form-item>
        <el-form-item label="优惠类型" required>
          <el-select v-model="form.couponType" class="w-full">
            <el-option label="立减/满减券" value="CASH" />
            <el-option label="折扣券" value="DISCOUNT" />
          </el-select>
        </el-form-item>
        <el-form-item label="减免额/折扣率" required>
          <el-input-number v-model="form.discountValue" :min="0.1" :precision="1" class="w-full" />
        </el-form-item>
        <el-form-item label="最低满减门槛" required>
          <el-input-number v-model="form.minThreshold" :min="0" class="w-full" />
        </el-form-item>
        <el-form-item label="发行总量" required>
          <el-input-number v-model="form.totalCount" :min="1" class="w-full" />
        </el-form-item>
        <el-form-item label="每人限领张数" required>
          <el-input-number v-model="form.perUserLimit" :min="1" class="w-full" />
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
import { CouponAPI, type CouponItem } from '@/services/coupon'

const list = ref<CouponItem[]>([])
const loading = ref(false)
const keyword = ref('')
const dialogVisible = ref(false)

const form = reactive<Partial<CouponItem>>({
  id: undefined,
  title: '',
  couponType: 'CASH',
  discountValue: 20,
  minThreshold: 100,
  totalCount: 1000,
  remainCount: 1000,
  perUserLimit: 1,
  status: 1
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await CouponAPI.getCouponList({ keyword: keyword.value })
    list.value = res.data?.list || [
      { id: 1001, title: '双11全场无门槛立减券', couponType: 'CASH', discountValue: 15, minThreshold: 0, totalCount: 2000, remainCount: 1450, perUserLimit: 1, status: 1, validStartTime: '2026-11-01', validEndTime: '2026-11-12', createTime: '2026-10-25' },
      { id: 1002, title: 'VIP会员专享8.8折优惠券', couponType: 'DISCOUNT', discountValue: 8.8, minThreshold: 200, totalCount: 500, remainCount: 88, perUserLimit: 2, status: 1, validStartTime: '2026-08-01', validEndTime: '2026-12-31', createTime: '2026-07-28' }
    ]
  } catch {
    // 拦截器捕获
  } finally {
    loading.value = false
  }
}

const handleOpenAdd = () => {
  Object.assign(form, { id: undefined, title: '', couponType: 'CASH', discountValue: 20, minThreshold: 100, totalCount: 1000, remainCount: 1000, perUserLimit: 1, status: 1 })
  dialogVisible.value = true
}

const handleEdit = (row: CouponItem) => {
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSave = async () => {
  try {
    await CouponAPI.saveCoupon(form)
    dialogVisible.value = false
    fetchData()
  } catch {
    // 拦截器捕获
  }
}

const handleToggleStatus = async (row: CouponItem) => {
  const nextStatus = row.status === 1 ? 0 : 1
  const actionText = nextStatus === 0 ? '封禁' : '解封'
  ElMessageBox.confirm(`确定要${actionText}优惠券 [${row.title}] 吗？`, '警告', { type: 'warning' }).then(async () => {
    try {
      await CouponAPI.toggleCouponStatus(row.id, nextStatus)
      fetchData()
    } catch {
      // 拦截器捕获
    }
  }).catch(() => {})
}

const handleDelete = (row: CouponItem) => {
  ElMessageBox.confirm(`确定要彻底删除优惠券 [${row.title}] 吗？`, '删除确认', { type: 'error' }).then(async () => {
    try {
      await CouponAPI.deleteCoupon(row.id)
      fetchData()
    } catch {
      // 拦截器捕获
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchData()
})
</script>
