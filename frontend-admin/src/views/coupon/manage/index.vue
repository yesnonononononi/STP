<template>
  <div class="space-y-6 animate-fade-in text-slate-800">
    <div class="p-6 rounded-xl border border-slate-200 bg-white shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-4">
      <div>
        <h3 class="text-xl font-semibold text-slate-800">优惠券管理</h3>
        <p class="text-sm text-slate-500 mt-1">创建与编辑满减券/折扣券，全量对齐后端 Coupon 领域模型的适用范围与有效期规则枚举。</p>
      </div>

      <div class="flex items-center gap-3">
        <el-input v-model="keyword" placeholder="搜索优惠券名称..." clearable class="w-64" @keyup.enter="fetchData" />
        <el-button type="primary" class="admin-btn-primary" @click="handleOpenAdd">
          + 新增优惠券
        </el-button>
        <el-button class="admin-btn-secondary" @click="fetchData">刷新</el-button>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="rounded-xl border border-slate-200 bg-white shadow-xs overflow-hidden p-6">
      <el-table v-loading="loading" :data="list" class="admin-table-custom" style="width: 100%">
        <el-table-column prop="id" label="券 ID" width="80" />

        <el-table-column label="图标/封面" width="90">
          <template #default="{ row }">
            <el-avatar v-if="row.image" :src="row.image" shape="square" :size="40" class="border border-slate-100 shadow-xs" />
            <div v-else class="w-10 h-10 rounded bg-slate-100 flex items-center justify-center text-slate-400 text-xs font-semibold">
              券
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="name" label="优惠券名称" min-width="160">
          <template #default="{ row }">
            <div class="font-semibold text-slate-800">{{ row.name }}</div>
            <div v-if="row.description" class="text-xs text-slate-400 truncate max-w-xs" :title="row.description">
              {{ row.description }}
            </div>
          </template>
        </el-table-column>

        <el-table-column label="优惠类型 / 面额" min-width="160">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <el-tag :type="row.type === 1 ? 'danger' : 'warning'" effect="plain">
                {{ row.type === 1 ? '满减/金额券' : '折扣券' }}
              </el-tag>
              <span class="font-bold text-rose-600">
                {{ row.type === 1 ? `￥${row.amount ?? 0}` : `${formatDiscountDisplay(row.discount)} 折` }}
              </span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="适用范围 " min-width="160">
          <template #default="{ row }">
            <el-tag :type="getScopeTypeTag(row.scopeType)" effect="light">
              {{ getScopeTypeName(row.scopeType) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="有效规则 " min-width="180">
          <template #default="{ row }">
            <div class="text-xs text-slate-600 space-y-0.5">
              <span v-if="row.timeType === 3" class="inline-block px-1.5 py-0.5 rounded bg-indigo-50 text-indigo-600 font-medium">
                按小时: {{ row.validHours ?? '-' }} 小时
              </span>
              <span v-else-if="row.timeType === 2" class="inline-block px-1.5 py-0.5 rounded bg-blue-50 text-blue-600 font-medium">
                按天数: {{ row.validDays ?? '-' }} 天
              </span>
              <span v-else class="inline-block px-1.5 py-0.5 rounded bg-slate-100 text-slate-600 font-medium">
                固定时间段
              </span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="创建 / 更新时间" min-width="180">
          <template #default="{ row }">
            <div class="text-xs text-slate-500 space-y-0.5">
              <div>创建: {{ formatTime(row.createTime) }}</div>
              <div v-if="row.updateTime">更新: {{ formatTime(row.updateTime) }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '已封禁' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" fixed="right">
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

    <!-- 悬浮表单对话框-->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑优惠券' : '新增优惠券'" width="620px" destroy-on-close>
      <el-form :model="form" label-width="140px" class="space-y-4">
        <el-form-item label="优惠券名称" required>
          <el-input v-model="form.name" placeholder="如：新人无门槛立减券" />
        </el-form-item>

        <el-form-item label="优惠券图标 URL">
          <el-input v-model="form.image" placeholder="请输入图片 HTTP/HTTPS 链接地址..." clearable />
        </el-form-item>

        <el-form-item label="优惠类型 (type)" required>
          <el-select v-model="form.type" class="w-full">
            <el-option label="立减 / 金额券 (1)" :value="1" />
            <el-option label="折扣券 (0)" :value="0" />
          </el-select>
        </el-form-item>

        <el-form-item v-if="form.type === 1" label="减免金额 (￥)" required>
          <el-input-number v-model="form.amount" :min="0" :precision="2" class="w-full" />
        </el-form-item>

        <el-form-item v-else label="折扣率 (折)" required>
          <el-input-number v-model="form.discount" :min="0.1" :max="9.9" :precision="1" class="w-full" placeholder="默认 8.5 折" />
        </el-form-item>

        <el-form-item label="适用范围 " required>
          <el-select v-model="form.scopeType" class="w-full">
            <el-option label="全场通用" :value="1" />
            <el-option label="指定商品分类" :value="2" />
            <el-option label="指定商品单品 " :value="3" />
          </el-select>
        </el-form-item>

        <el-form-item label="启用/使用状态" required>
          <el-select v-model="form.status" class="w-full">
            <el-option label="正常 / 启用 " :value="1" />
            <el-option label="禁用 / 封禁 " :value="0" />
          </el-select>
        </el-form-item>

        <el-form-item label="有效时间规则 " required>
          <el-select v-model="form.timeType" class="w-full">
            <el-option label="固定时间段" :value="1" />
            <el-option label="按天生效 " :value="2" />
            <el-option label="按小时生效 " :value="3" />
          </el-select>
        </el-form-item>

        <el-form-item v-if="form.timeType === 3" label="有效小时数" required>
          <el-input-number v-model="form.validHours" :min="1" class="w-full" placeholder="例如：24 小时" />
        </el-form-item>

        <el-form-item v-else-if="form.timeType === 2" label="有效天数" required>
          <el-input-number v-model="form.validDays" :min="1" class="w-full" placeholder="例如：7 天" />
        </el-form-item>

        <el-form-item label="使用说明描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="填写优惠券使用细则说明..." />
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
import { CouponAPI, type AdminCouponVO, type CreateCouponPayload } from '@/services/coupon'

const list = ref<AdminCouponVO[]>([])
const loading = ref(false)
const keyword = ref('')
const total = ref(0)
const pagination = reactive({ page: 1, pageSize: 10 })
const dialogVisible = ref(false)

// 使用 ref 对象覆盖对齐 Coupon 领域模型枚举
const form = ref<CreateCouponPayload>({
  id: undefined,
  name: '',
  image: '',
  type: 1,
  amount: 10,
  discount: 8.5,
  scopeType: 1, // ALL_SCOPE = 1
  status: 1,
  timeType: 2,  // RECEIVE_EFFECT_TIME_PERIOD_BY_DAY = 2
  validDays: 7,
  validHours: 24,
  description: ''
})

const getScopeTypeName = (type?: number) => {
  if (type === 1) return '全场通用'
  if (type === 2) return '指定商品分类'
  if (type === 3) return '指定商品单品'
  return '全场通用'
}

const getScopeTypeTag = (type?: number) => {
  if (type === 1) return 'success'
  if (type === 2) return 'warning'
  if (type === 3) return 'danger'
  return 'info'
}

const formatDiscountDisplay = (val?: number): string => {
  if (val == null) return '10'
  if (val <= 1) {
    return (val * 10).toFixed(1).replace(/\.0$/, '')
  }
  return String(val)
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
    const res = await CouponAPI.getCouponList({
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

// 打开新增弹窗 (重置全量属性, 默认 ALL_SCOPE = 1, BY_DAY = 2)
const handleOpenAdd = () => {
  form.value = {
    id: undefined,
    name: '',
    image: '',
    type: 1,
    amount: 10,
    discount: 8.5,
    scopeType: 1,
    status: 1,
    timeType: 2,
    validDays: 7,
    validHours: 24,
    description: ''
  }
  dialogVisible.value = true
}

// 打开编辑更新弹窗 (全量回显与类型精准强转换)
const handleEdit = (row: AdminCouponVO) => {
  let discountVal = row.discount ?? 8.5
  if (discountVal <= 1) {
    discountVal = Number((discountVal * 10).toFixed(1))
  }
  form.value = {
    id: row.id != null ? Number(row.id) : undefined,
    name: row.name || '',
    image: row.image || '',
    type: row.type != null ? Number(row.type) : 1,
    amount: row.amount != null ? Number(row.amount) : 10,
    discount: discountVal,
    scopeType: row.scopeType != null ? Number(row.scopeType) : 1,
    status: row.status != null ? Number(row.status) : 1,
    timeType: row.timeType != null ? Number(row.timeType) : 2,
    validDays: row.validDays != null ? Number(row.validDays) : 7,
    validHours: row.validHours != null ? Number(row.validHours) : 24,
    description: row.description || ''
  }
  dialogVisible.value = true
}

// 确认保存：根据所选 type 与 timeType 精准构造全量 Payload 提交
const handleSave = async () => {
  if (!form.value.name.trim()) {
    ElMessage.warning('请输入优惠券名称')
    return
  }
  try {
    let convertedDiscount: number | undefined = undefined
    if (form.value.type === 0 && form.value.discount != null) {
      convertedDiscount = form.value.discount <= 10 ? Number((form.value.discount / 10).toFixed(4)) : form.value.discount
    }

    const payload: CreateCouponPayload = {
      id: form.value.id != null ? Number(form.value.id) : undefined,
      name: form.value.name.trim(),
      image: form.value.image ? form.value.image.trim() : undefined,
      type: Number(form.value.type ?? 1),
      amount: form.value.type === 1 ? (form.value.amount != null ? Number(form.value.amount) : undefined) : undefined,
      discount: convertedDiscount,
      scopeType: Number(form.value.scopeType ?? 1),
      status: Number(form.value.status ?? 1),
      timeType: Number(form.value.timeType ?? 2),
      validDays: form.value.timeType === 2 ? (form.value.validDays ? Number(form.value.validDays) : 7) : undefined,
      validHours: form.value.timeType === 3 ? (form.value.validHours ? Number(form.value.validHours) : 24) : undefined,
      description: form.value.description ? form.value.description.trim() : undefined,
    }
    await CouponAPI.saveCoupon(payload)
    ElMessage.success(form.value.id ? '更新成功' : '新增成功')
    dialogVisible.value = false
    fetchData()
  } catch {
    // 拦截器处理
  }
}

const handleToggleStatus = async (row: AdminCouponVO) => {
  const isBan = row.status === 1
  const actionText = isBan ? '封禁' : '解封'
  ElMessageBox.confirm(`确定要${actionText}优惠券 [${row.name}] 吗？`, '提示', { type: 'warning' }).then(async () => {
    try {
      if (isBan) {
        await CouponAPI.banCoupon(row.id)
      } else {
        await CouponAPI.unbanCoupon(row.id)
      }
      ElMessage.success(`${actionText}成功`)
      fetchData()
    } catch {
      // 拦截器处理
    }
  }).catch(() => {})
}

const handleDelete = (row: AdminCouponVO) => {
  ElMessageBox.confirm(`确定要删除优惠券 [${row.name}] 吗？`, '删除确认', { type: 'error' }).then(async () => {
    try {
      await CouponAPI.deleteCoupon(row.id)
      ElMessage.success('删除成功')
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
