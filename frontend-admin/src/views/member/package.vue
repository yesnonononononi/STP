<template>
  <div class="space-y-6 animate-fade-in text-slate-800">
    <div class="p-6 rounded-xl border border-slate-200 bg-white shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-4">
      <div>
        <h3 class="text-xl font-semibold text-slate-800">会员套餐配置</h3>
        <p class="text-sm text-slate-500 mt-1">配置会员售卖套餐（月卡、季卡、年卡）、定价、有效时长、状态及库存数量。</p>
      </div>

      <div class="flex items-center gap-3">
        <el-button type="primary" class="admin-btn-primary" @click="handleOpenAdd">
          + 新增套餐
        </el-button>
        <el-button class="admin-btn-secondary" @click="fetchData">刷新</el-button>
      </div>
    </div>

    <!-- 数据列表 -->
    <div class="rounded-xl border border-slate-200 bg-white shadow-xs overflow-hidden p-6">
      <el-table v-loading="loading" :data="list" class="admin-table-custom" style="width: 100%">
        <el-table-column prop="id" label="套餐 ID" width="90" />

        <el-table-column prop="name" label="套餐名称" min-width="160">
          <template #default="{ row }">
            <span class="font-semibold text-slate-800">{{ row.name }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="price" label="售价" min-width="120">
          <template #default="{ row }">
            <span class="font-bold text-rose-600 font-mono">￥{{ row.price?.toFixed?.(2) ?? row.price }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="duration" label="有效时长" width="120">
          <template #default="{ row }">
            <el-tag type="info">{{ row.duration }} 天</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="typeName" label="关联类型" width="150">
          <template #default="{ row }">
            <el-tag type="info">
              {{ getTypeName(row.typeId, row.typeName) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="stock" label="库存数量" width="120">
          <template #default="{ row }">
            <span class="font-mono" :class="row.stock < 50 ? 'text-amber-600 font-bold' : 'text-slate-700'">
              {{ row.stock }}
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="discount" label="折扣" width="110">
          <template #default="{ row }">
            <span v-if="row.discount != null">
              {{ formatDiscountDisplay(row.discount) }} 折
            </span>
            <span v-else>10 折</span>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '使用中' : '未使用' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="创建时间" min-width="170">
          <template #default="{ row }">
            <span class="text-xs text-slate-500">{{ formatTime(row.createTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="description" label="套餐描述" min-width="200" />

        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <el-button size="small" type="primary" plain @click="handleEdit(row)">编辑</el-button>
              <el-button size="small" type="danger" plain @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 对话框 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑会员套餐' : '新增会员套餐'" width="540px" destroy-on-close>
      <el-form :model="form" label-width="120px" class="space-y-4">
        <el-form-item label="套餐名称" required>
          <el-input v-model="form.name" placeholder="如：VIP 连续包月套餐" />
        </el-form-item>
        <el-form-item label="售价" required>
          <el-input-number v-model="form.price" :min="0" :precision="2" class="w-full" />
        </el-form-item>
        <el-form-item label="有效天数" required>
          <el-input-number v-model="form.duration" :min="1" class="w-full" />
        </el-form-item>
        <el-form-item label="会员类型" required>
          <el-select v-model="form.typeId" placeholder="请选择会员类型" class="w-full">
            <el-option
              v-for="item in memberTypeList"
              :key="item.id"
              :label="`${item.name}${item.description ? ' (' + item.description + ')' : ''}`"
              :value="Number(item.id)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="使用状态" required>
          <el-select v-model="form.status" class="w-full">
            <el-option label="使用中" :value="1" />
            <el-option label="未使用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="库存数量" required>
          <el-input-number v-model="form.stock" :min="0" class="w-full" />
        </el-form-item>
        <el-form-item label="折扣 (折)">
          <el-input-number v-model="form.discount" :min="0.1" :max="10" :precision="1" class="w-full" placeholder="默认 10 折 (如 8.5)" />
        </el-form-item>
        <el-form-item label="排序优先级">
          <el-input-number v-model="form.priority" :min="0" class="w-full" />
        </el-form-item>
        <el-form-item label="套餐描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="详细套餐权益说明..." />
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
import { MemberAPI, type MemberPackageItem, type MemberTypeVO } from '@/services/member'

const list = ref<MemberPackageItem[]>([])
const loading = ref(false)
const dialogVisible = ref(false)

const memberTypeList = ref<MemberTypeVO[]>([
  { id: 1, name: '普通会员', description: '尊享基础特权' },
  { id: 2, name: '超级会员', description: '畅享高级特权' }
])

const fetchMemberTypes = async () => {
  try {
    const res = await MemberAPI.getMemberTypeList()
    if (res && res.data && Array.isArray(res.data) && res.data.length > 0) {
      memberTypeList.value = res.data
    }
  } catch {
    // 允许降级使用默认映射列表
  }
}

const getTypeName = (typeId?: number, typeName?: string): string => {
  if (typeName) return typeName
  const found = memberTypeList.value.find((t) => Number(t.id) === Number(typeId))
  return found ? found.name : (typeId != null ? `类型 ID: ${typeId}` : '-')
}

const form = reactive<MemberPackageItem>({
  id: undefined,
  name: '',
  price: 25.0,
  duration: 30,
  description: '',
  typeId: 1,
  stock: 999,
  discount: 10,
  status: 1,
  priority: 1
})

const formatDiscountDisplay = (val: number): string => {
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
    const res = await MemberAPI.getPackageList()
    list.value = res.data || [
      { id: 1, name: 'VIP 连续包月套餐', price: 25.0, duration: 30, description: '自动续费，随时可退', typeId: 1, typeName: '普通会员', stock: 9999, discount: 0.85, status: 1, priority: 1, createTime: '2026-01-01 10:00:00' },
      { id: 2, name: 'VIP 年度至尊套餐', price: 198.0, duration: 365, description: '超值年度优惠，畅享全年特权', typeId: 2, typeName: '超级会员', stock: 500, discount: 0.7, status: 1, priority: 2, createTime: '2026-01-01 10:00:00' }
    ]
  } catch {
    // 拦截器处理
  } finally {
    loading.value = false
  }
}

const handleOpenAdd = () => {
  Object.assign(form, {
    id: undefined,
    name: '',
    price: 25.0,
    duration: 30,
    description: '',
    typeId: memberTypeList.value[0] ? Number(memberTypeList.value[0].id) : 1,
    stock: 999,
    discount: 10,
    status: 1,
    priority: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row: MemberPackageItem) => {
  Object.assign(form, row)
  if (form.status == null) form.status = 1
  // 若后端返回小数折扣（如 0.85），转为 10 折制展示（如 8.5）
  if (form.discount != null && form.discount <= 1) {
    form.discount = Number((form.discount * 10).toFixed(1))
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  try {
    // 传参时将用户输入的折扣 / 10 得到小数位折扣传入后端
    const payload: MemberPackageItem = {
      ...form,
      discount: form.discount != null ? Number((form.discount / 10).toFixed(4)) : 1.0
    }
    await MemberAPI.savePackage(payload)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchData()
  } catch {
    // 拦截器处理
  }
}

const handleDelete = (row: MemberPackageItem) => {
  ElMessageBox.confirm(`确定删除套餐 [${row.name}] 吗？`, '警告', { type: 'warning' }).then(async () => {
    if (row.id) {
      await MemberAPI.deletePackage(row.id)
      ElMessage.success('删除成功')
      fetchData()
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchMemberTypes()
  fetchData()
})
</script>
